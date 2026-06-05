# Biblio-Spring

API REST de gestion de bibliothèque avec Spring Boot 4, PostgreSQL, authentification JWT et Swagger.

---

## Prérequis

- **Java 17+** — vérifie avec `java -version`
- **PostgreSQL** — installé et en cours d'exécution
- **Maven** — ou utilise `./mvnw` fourni dans le projet

---

## 1. Créer la base de données

```bash
sudo -u postgres createdb bibliotheque
```

## 2. Configurer la connexion

Ouvre `BibliothequeApp/src/main/resources/application.properties` et modifie le mot de passe si besoin :

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bibliotheque
spring.datasource.username=postgres
spring.datasource.password=ton-mot-de-passe
```

## 3. Lancer l'application

```bash
cd BibliothequeApp
./mvnw spring-boot:run
```

L'application démarre sur **http://localhost:8081** et se connecte à PostgreSQL.

> L'application crée automatiquement 2 comptes de test au premier démarrage (voir plus bas).

---

## Tester l'API (curl)

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

Le mot de passe dans `application.properties` est incorrect.

```bash
# 1. Démarre PostgreSQL
sudo systemctl start postgresql
pg_isready

# 2. Définis le bon mot de passe
sudo -u postgres psql -c "ALTER USER postgres PASSWORD '36303631Hb';"

# 3. Vérifie que la valeur est bien dans application.properties
```

### `FATAL: database "bibliotheque" does not exist`

```bash
sudo -u postgres createdb bibliotheque
```

### `Connection refused` (port 5432)

PostgreSQL n'est pas démarré :

```bash
sudo systemctl start postgresql
pg_isready
```

### `Port 8081 already in use`

```bash
sudo lsof -ti:8081 | xargs kill -9
# ou change : server.port=8082 dans application.properties
```

### Erreurs de compilation

```bash
./mvnw clean install -U
```

### `401 Unauthorized`

Token absent ou expiré → reconnecte-toi :

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bibliotheque.com","motDePasse":"admin123"}'
```

### `403 Forbidden` sur POST/PUT/DELETE

Le compte n'est pas ADMIN. Connecte-toi avec `admin@bibliotheque.com` / `admin123`.

---

## Sécurité

Voir [SECURITY-README.md](./SECURITY-README.md) pour tous les détails (flux JWT, rôles, configuration).
