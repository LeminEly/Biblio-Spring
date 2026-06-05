# Biblio-Spring

API REST de gestion de bibliothèque avec Spring Boot 4, PostgreSQL, authentification JWT et Swagger.

---

## Prérequis

- **Java 17+** — vérifie avec `java -version`
- **Maven** — ou utilise `./mvnw` fourni dans le projet

---

## 1. Lancer l'application

```bash
cd BibliothequeApp
./mvnw spring-boot:run
```

L'application démarre sur **http://localhost:8081**.

> **Par défaut**, l'application utilise une base de données **H2 en mémoire** (aucune installation requise).
> Pour utiliser **PostgreSQL** (production), voir la section [Passer en PostgreSQL](#passer-en-postgresql).

---

## 2. Utiliser l'application PostgreSQL (optionnel)

Si tu veux utiliser PostgreSQL au lieu de H2 :

**a. Assure-toi que PostgreSQL est démarré :**
```bash
sudo systemctl start postgresql
pg_isready
```

**b. Crée la base de données :**
```bash
sudo -u postgres createdb bibliotheque
# ou si tu es déjà dans psql : CREATE DATABASE bibliotheque;
```

**c. Modifie le profil actif dans `application.properties` :**
```properties
# Remplace cette ligne :
spring.profiles.active=dev

# Par celle-ci :
# spring.profiles.active=postgres
```

**d. Crée le fichier `application-postgres.properties` :**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bibliotheque
spring.datasource.username=postgres
spring.datasource.password=ton-mot-de-passe
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**e. Relance l'application.**

---

## Tester l'API (curl)

> Les commandes ci-dessous fonctionnent immédiatement après le lancement.
> L'application crée automatiquement 2 comptes de test au démarrage.

L'application crée automatiquement 2 comptes de test au démarrage :

| Email | Mot de passe | Rôle |
|---|---|---|
| `admin@bibliotheque.com` | `admin123` | ADMIN |
| `user@bibliotheque.com` | `user123` | USER |

### 1. Connexion → récupérer le token

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bibliotheque.com","motDePasse":"admin123"}'
```

**Réponse :**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "admin@bibliotheque.com",
  "role": "ADMIN"
}
```

### 2. Utiliser le token pour les endpoints protégés

Remplace `<TOKEN>` par le token reçu :

```bash
# Lister les livres
curl http://localhost:8081/livres -H "Authorization: Bearer <TOKEN>"

# Lister les auteurs
curl http://localhost:8081/auteurs -H "Authorization: Bearer <TOKEN>"

# Créer un auteur (admin seulement)
curl -X POST http://localhost:8081/auteurs \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"nom":"George Orwell"}'

# Créer un livre (admin seulement)
curl -X POST http://localhost:8081/livres \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"titre":"1984","isbn":"978-0451524935","anneePublication":1949,"auteur":{"id":1}}'

# Inscription d'un nouvel utilisateur
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nom":"nouvelutilisateur","email":"test@test.com","motDePasse":"password123"}'
```

### 3. Script complet en une ligne

```bash
# Connexion et sauvegarde du token dans une variable
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bibliotheque.com","motDePasse":"admin123"}' | \
  python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# Test : lister les livres
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/livres | python3 -m json.tool
```

---

## Swagger UI

| Ressource | URL |
|---|---|
| Interface Swagger | http://localhost:8081/swagger-ui/index.html |
| Documentation JSON | http://localhost:8081/v3/api-docs |

**Utilisation :** Ouvre Swagger UI → clique sur **Authorize** → entre `Bearer <ton-token>` → teste les endpoints.

---

## Commandes utiles

```bash
cd BibliothequeApp

# Compiler
./mvnw clean compile

# Lancer les tests
./mvnw test

# Packager en JAR
./mvnw clean package -DskipTests

# Lancer le JAR
java -jar target/BibliothequeApp-0.0.1-SNAPSHOT.jar
```

---

## Structure du projet

```
BibliothequeApp/
├── src/main/java/com/Bibliotheque/BibliothequeApp/
│   ├── BibliothequeAppApplication.java    # Point d'entrée
│   ├── config/
│   │   ├── DataInitializer.java           # Crée les comptes de test
│   │   └── OpenApiConfig.java             # Configuration Swagger
│   ├── controller/                        # Endpoints REST
│   │   ├── AuteurController.java
│   │   └── LivreController.java
│   ├── security/
│   │   ├── config/SecurityConfig.java     # Règles de sécurité
│   │   ├── jwt/                           # JWT (token, filtre, erreur 401)
│   │   ├── service/                       # Chargement des utilisateurs
│   │   ├── dto/                           # Login/Register/Response
│   │   └── controller/AuthController.java # /api/auth/login & register
│   ├── model/                             # Entités JPA
│   │   ├── Auteur.java
│   │   ├── Livre.java
│   │   └── Utilisateur.java
│   ├── repository/                        # Accès base de données
│   ├── services/                          # Logique métier
│   └── exception/                         # Gestion des erreurs
└── src/main/resources/
    └── application.properties             # Configuration
```

---

## Dépannage / Erreurs fréquentes

### `FATAL: password authentication failed for user "postgres"`

**Cause :** Le mot de passe PostgreSQL dans `application.properties` est incorrect.

**Solution :**
```bash
# 1. Vérifie que PostgreSQL est démarré
sudo systemctl start postgresql

# 2. Connecte-toi et définis un nouveau mot de passe
sudo -u postgres psql -c "ALTER USER postgres PASSWORD '36303631Hb';"

# 3. Copie ce mot de passe exact dans application.properties
```

### `FATAL: database "bibliotheque" does not exist`

**Cause :** La base de données n'a pas été créée.

**Solution :**
```bash
sudo -u postgres createdb bibliotheque
```

### `Port 8081 already in use`

**Cause :** Un autre processus utilise déjà le port 8081.

**Solution :**
```bash
# Changer de port dans application.properties :
server.port=8082

# Ou tuer le processus qui bloque le port :
sudo lsof -ti:8081 | xargs kill -9
```

### `java.net.ConnectException: Connection refused`

**Cause :** PostgreSQL n'est pas en cours d'exécution.

**Solution :**
```bash
# Démarrer PostgreSQL
sudo systemctl start postgresql

# Vérifier qu'il tourne
pg_isready
```

### L'application ne démarre pas car le port 8081 est déjà utilisé

Voir l'erreur `Port 8081 already in use` ci-dessous.

### `Field datasource in ... required a bean of type 'javax.sql.DataSource'`

**Cause :** Le profil actif pointe vers PostgreSQL mais PostgreSQL n'est pas disponible.

**Solution :** Utilise le profil H2 (aucune installation nécessaire) :
```properties
# Dans application.properties
spring.profiles.active=dev
```

### `package javax.servlet does not exist` ou erreurs de compilation

**Cause :** Maven n'a pas téléchargé toutes les dépendances.

**Solution :**
```bash
# Nettoyer et forcer le re-téléchargement
./mvnw clean install -U
```

### L'application compile mais les requêtes retournent `401 Unauthorized`

**Cause :** Token JWT manquant, invalide ou expiré.

**Solution :**
```bash
# Reconnecte-toi pour obtenir un nouveau token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bibliotheque.com","motDePasse":"admin123"}'

# Utilise le nouveau token dans le header
curl http://localhost:8081/livres -H "Authorization: Bearer <nouveau-token>"
```

### `403 Forbidden` sur POST/PUT/DELETE

**Cause :** Le compte utilisé n'a pas le rôle ADMIN.

**Solution :** Connecte-toi avec le compte admin (`admin@bibliotheque.com` / `admin123`) ou inscris un nouveau compte et modifie son rôle en `ADMIN` dans la base de données.

---

## Sécurité

Voir [SECURITY-README.md](./SECURITY-README.md) pour tous les détails (flux JWT, rôles, configuration).
