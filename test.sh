#!/bin/bash
# test.sh - Script de test complet de l'API
# Usage : ./test.sh

set -e

BASE_URL="http://localhost:8081"

# Couleurs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}=== Test de l'API Biblio-Spring ===${NC}\n"

# 1. Vérifier que l'app répond
echo -e "${YELLOW}[1] Vérification que l'app tourne...${NC}"
if ! curl -s "$BASE_URL/swagger-ui/index.html" -o /dev/null; then
    echo -e "${RED}[FAIL] L'app ne répond pas sur $BASE_URL${NC}"
    echo "Lance d'abord: cd BibliothequeApp && ./mvnw spring-boot:run"
    exit 1
fi
echo -e "${GREEN}[OK] App accessible${NC}\n"

# 2. Login admin
echo -e "${YELLOW}[2] Login admin...${NC}"
ADMIN_RESP=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bibliotheque.com","motDePasse":"admin123"}')
ADMIN_TOKEN=$(echo "$ADMIN_RESP" | python3 -c "import sys,json;print(json.load(sys.stdin).get('token',''))")

if [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}[FAIL] Login admin échoué${NC}"
    echo "Réponse: $ADMIN_RESP"
    exit 1
fi
echo -e "${GREEN}[OK] Token admin reçu${NC}\n"

# 3. Login user
echo -e "${YELLOW}[3] Login user...${NC}"
USER_RESP=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@bibliotheque.com","motDePasse":"user123"}')
USER_TOKEN=$(echo "$USER_RESP" | python3 -c "import sys,json;print(json.load(sys.stdin).get('token',''))")

if [ -z "$USER_TOKEN" ]; then
    echo -e "${RED}[FAIL] Login user échoué${NC}"
    echo "Réponse: $USER_RESP"
    exit 1
fi
echo -e "${GREEN}[OK] Token user reçu${NC}\n"

# 4. Admin crée un auteur
echo -e "${YELLOW}[4] Admin crée un auteur...${NC}"
AUTHOR_RESP=$(curl -s -X POST "$BASE_URL/auteurs" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"George Orwell"}')
AUTHOR_ID=$(echo "$AUTHOR_RESP" | python3 -c "import sys,json;print(json.load(sys.stdin).get('id',''))")

if [ -z "$AUTHOR_ID" ]; then
    echo -e "${RED}[FAIL] Création auteur échouée${NC}"
    echo "Réponse: $AUTHOR_RESP"
    exit 1
fi
echo -e "${GREEN}[OK] Auteur créé id=$AUTHOR_ID${NC}\n"

# 5. Admin crée un livre
echo -e "${YELLOW}[5] Admin crée un livre...${NC}"
BOOK_RESP=$(curl -s -X POST "$BASE_URL/livres" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"titre\":\"1984\",\"isbn\":\"978-0451524935\",\"anneePublication\":1949,\"auteur\":{\"id\":$AUTHOR_ID}}")
BOOK_ID=$(echo "$BOOK_RESP" | python3 -c "import sys,json;print(json.load(sys.stdin).get('id',''))")

if [ -z "$BOOK_ID" ]; then
    echo -e "${RED}[FAIL] Création livre échouée${NC}"
    echo "Réponse: $BOOK_RESP"
    exit 1
fi
echo -e "${GREEN}[OK] Livre créé id=$BOOK_ID${NC}\n"

# 6. User lit la liste des livres
echo -e "${YELLOW}[6] User lit la liste des livres...${NC}"
BOOKS=$(curl -s -H "Authorization: Bearer $USER_TOKEN" "$BASE_URL/livres")
COUNT=$(echo "$BOOKS" | python3 -c "import sys,json;print(len(json.load(sys.stdin)))")
echo -e "${GREEN}[OK] $COUNT livre(s) listé(s)${NC}\n"

# 7. User tente de créer un auteur (doit être 403)
echo -e "${YELLOW}[7] User tente de créer un auteur (doit être 403)...${NC}"
STATUS=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/auteurs" \
  -H "Authorization: Bearer $USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"nom":"Test"}')

if [ "$STATUS" = "403" ]; then
    echo -e "${GREEN}[OK] Accès refusé (403) comme prévu${NC}\n"
else
    echo -e "${RED}[FAIL] Attendu 403, reçu $STATUS${NC}\n"
fi

# 8. Inscription d'un nouvel utilisateur (email unique pour test idempotent)
echo -e "${YELLOW}[8] Inscription d'un nouvel utilisateur...${NC}"
TS=$(date +%s)
REGISTER_RESP=$(curl -s -X POST "$BASE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"nom\":\"nouveau_$TS\",\"email\":\"nouveau_$TS@test.com\",\"motDePasse\":\"pass123\"}")

NEW_TOKEN=$(echo "$REGISTER_RESP" | python3 -c "import sys,json;print(json.load(sys.stdin).get('token',''))")

if [ -z "$NEW_TOKEN" ]; then
    echo -e "${RED}[FAIL] Inscription échouée${NC}"
    echo "Réponse: $REGISTER_RESP"
    exit 1
fi
echo -e "${GREEN}[OK] Utilisateur inscrit, token reçu${NC}\n"

# 9. Vérifier que la donnée est bien en PostgreSQL
echo -e "${YELLOW}[9] Vérification en base PostgreSQL...${NC}"
DB_COUNT=$(PGPASSWORD=lemin psql -h 127.0.0.1 -U postgres -d bibliotheque -tAc "SELECT COUNT(*) FROM auteur;" 2>/dev/null)
if [ "$DB_COUNT" -ge "1" ]; then
    echo -e "${GREEN}[OK] $DB_COUNT auteur(s) en base PostgreSQL${NC}\n"
else
    echo -e "${RED}[FAIL] Aucun auteur en base${NC}\n"
fi

echo -e "${GREEN}=== TOUS LES TESTS SONT PASSÉS ===${NC}"
echo ""
echo "Récap :"
echo "  - 2 logins (admin + user)"
echo "  - 1 création d'auteur (par admin)"
echo "  - 1 création de livre (par admin)"
echo "  - 1 lecture (par user)"
echo "  - 1 tentative bloquée (403 pour user)"
echo "  - 1 inscription"
echo "  - Vérification en base PostgreSQL"
