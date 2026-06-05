# Biblio-Spring

API REST de gestion de bibliothèque développée avec **Spring Boot 4.0.5**, **PostgreSQL**, **Spring Security 7**, authentification par **JWT** et documentation **Swagger/OpenAPI**.

> Base de données : **PostgreSQL uniquement**. Aucun autre SGBD n'est supporté.

---

## Sommaire

1. [Stack technique](#stack-technique)
2. [Prérequis](#prérequis)
3. [Installation de PostgreSQL](#installation-de-postgresql)
4. [Configuration du projet](#configuration-du-projet)
5. [Lancement de l'application](#lancement-de-lapplication)
6. [Comptes de test](#comptes-de-test)
7. [Documentation de l'API](#documentation-de-lapi)
8. [Endpoints REST](#endpoints-rest)
9. [Tests automatisés](#tests-automatisés)
10. [Commandes Maven utiles](#commandes-maven-utiles)
11. [Structure du projet](#structure-du-projet)
12. [Sécurité](#sécurité)
13. [Dépannage](#dépannage)

---

## Stack technique

| Technologie | Version | Rôle |
|---|---|---|
| Java | 17 | Langage |
| Spring Boot | 4.0.5 | Framework |
| Spring Security | 7.x | Authentification / autorisation |
| Spring Data JPA | 4.x | Persistance |
| PostgreSQL | 18+ | Base de données relationnelle |
| JJWT | 0.12.6 | Tokens JWT (HMAC-SHA512) |
| BCrypt | Intégré à Spring Security | Hachage des mots de passe |
| springdoc-openapi | 3.0.3 | Swagger UI / OpenAPI 3 |

---

## Prérequis

Avant de commencer, assure-toi d'avoir installé :

- **Java JDK 17+** — vérifie avec `java -version`
- **PostgreSQL 18+** — serveur démarré et accessible
- **Maven** (optionnel) — le projet inclut `./mvnw` (Maven Wrapper)
- **curl** + **python3** — pour les tests automatisés

---

## Installation de PostgreSQL

### Debian / Ubuntu / Kali

```bash
# Installation
sudo apt update
sudo apt install -y postgresql postgresql-contrib

# Démarrage du service
sudo systemctl start postgresql
sudo systemctl enable postgresql   # démarrage auto au boot

# Vérification
pg_isready
# Attendu : /var/run/postgresql:5432 - accepting connections
```

### Création de la base de données

```bash
# 1. Définir un mot de passe pour l'utilisateur postgres (si pas déjà fait)
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'lemin';"

# 2. Créer la base "bibliotheque"
sudo -u postgres psql -c "CREATE DATABASE bibliotheque;"

# 3. Vérifier la connexion
PGPASSWORD=lemin psql -h 127.0.0.1 -U postgres -d bibliotheque -c "SELECT version();"
```

> **Note** : remplace `lemin` par le mot de passe de ton choix, mais pense à le reporter dans `application.properties` (étape suivante).

### macOS (Homebrew)

```bash
brew install postgresql@18
brew services start postgresql@18
createuser -s postgres       # si nécessaire
createdb bibliotheque
```

---

## Configuration du projet

Ouvre **`BibliothequeApp/src/main/resources/application.properties`** et vérifie ces lignes :

```properties
# === Connexion PostgreSQL ===
spring.datasource.url=jdbc:postgresql://localhost:5432/bibliotheque
spring.datasource.username=postgres
spring.datasource.password=lemin          # <-- adapte ce mot de passe

# === JPA / Hibernate ===
spring.jpa.hibernate.ddl-auto=update       # crée/met à jour les tables automatiquement
spring.jpa.show-sql=true

# === JWT ===
jwt.secret=<ta-clé-secrète-base64>         # change en production (voir Sécurité)
jwt.expiration=86400000                    # 24h en millisecondes
```

> Le port par défaut de l'application est **8081** (modifiable via `server.port`).

---

## Lancement de l'application

```bash
# Depuis la racine du projet
cd BibliothequeApp

# Avec le wrapper Maven
./mvnw spring-boot:run

# OU avec Maven installé globalement
mvn spring-boot:run
```

**Sortie attendue :**

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

 :: Spring Boot ::                (v4.0.5)

... Tomcat started on port 8081 (http)
... Started BibliothequeAppApplication in X.XXX seconds
```

L'application crée automatiquement les tables `auteur`, `livre` et `utilisateurs` dans PostgreSQL, ainsi que deux comptes de test (voir ci-dessous).

Accessible sur : **http://localhost:8081**

---

## Comptes de test

Deux comptes sont créés automatiquement par `DataInitializer` au premier démarrage :

| Email | Mot de passe | Rôle | Droits |
|---|---|---|---|
| `admin@bibliotheque.com` | `admin123` | `ADMIN` | Lecture + écriture (CRUD complet) |
| `user@bibliotheque.com` | `user123` | `USER` | Lecture seule (GET) |

> ⚠️ Ces comptes sont destinés au développement uniquement. **Change-les en production** (voir section Sécurité).

---

## Documentation de l'API

| Ressource | URL |
|---|---|
| **Interface Swagger UI** | http://localhost:8081/swagger-ui/index.html |
| **Spec OpenAPI (JSON)** | http://localhost:8081/v3/api-docs |

**Tester un endpoint protégé dans Swagger :**

1. Connecte-toi d'abord via `POST /api/auth/login` (curl ou Swagger)
2. Récupère le `token` de la réponse
3. Clique sur le bouton **Authorize** (🔓) en haut de Swagger UI
4. Entre : `Bearer <ton-token>`
5. Tous les appels Swagger seront automatiquement authentifiés

---

## Endpoints REST

### Authentification (publics)

| Méthode | Endpoint | Description | Body |
|---|---|---|---|
| `POST` | `/api/auth/login` | Connexion → retourne un JWT | `{ "email", "motDePasse" }` |
| `POST` | `/api/auth/register` | Inscription (rôle USER par défaut) | `{ "nom", "email", "motDePasse" }` |

### Livres

| Méthode | Endpoint | Rôle requis | Description |
|---|---|---|---|
| `GET` | `/livres` | Tout authentifié | Liste tous les livres |
| `GET` | `/livres/{id}` | Tout authentifié | Détail d'un livre |
| `POST` | `/livres` | **ADMIN** | Créer un livre |
| `PUT` | `/livres/{id}` | **ADMIN** | Modifier un livre |
| `DELETE` | `/livres/{id}` | **ADMIN** | Supprimer un livre |

### Auteurs

| Méthode | Endpoint | Rôle requis | Description |
|---|---|---|---|
| `GET` | `/auteurs` | Tout authentifié | Liste tous les auteurs |
| `GET` | `/auteurs/{id}` | Tout authentifié | Détail d'un auteur |
| `POST` | `/auteurs` | **ADMIN** | Créer un auteur |
| `PUT` | `/auteurs/{id}` | **ADMIN** | Modifier un auteur |
| `DELETE` | `/auteurs/{id}` | **ADMIN** | Supprimer un auteur |

### Codes d'erreur

| Code | Signification | Cause |
|---|---|---|
| `401 Unauthorized` | Non authentifié | Token absent, invalide ou expiré |
| `403 Forbidden` | Authentifié mais sans droits | Rôle insuffisant (USER tentant une action ADMIN) |
| `400 Bad Request` | Requête invalide | Email/nom déjà utilisé, champ manquant |
| `404 Not Found` | Ressource introuvable | ID inexistant |

---

## Tests automatisés

Un script `test.sh` à la racine vérifie l'ensemble de l'API en 9 étapes (login, CRUD, RBAC, persistance PostgreSQL).

```bash
# Depuis la racine du projet
./test.sh
```

**Sortie attendue :**

```
[1] Vérification que l'app tourne...         [OK]
[2] Login admin...                           [OK]
[3] Login user...                            [OK]
[4] Admin crée un auteur...                  [OK]
[5] Admin crée un livre...                   [OK]
[6] User lit la liste des livres...          [OK]
[7] User tente de créer un auteur (403)...   [OK]
[8] Inscription d'un nouvel utilisateur...   [OK]
[9] Vérification en base PostgreSQL...       [OK]

=== TOUS LES TESTS SONT PASSÉS ===
```

Le script est **idempotent** : il peut être relancé plusieurs fois sans erreur.

---

## Commandes Maven utiles

```bash
cd BibliothequeApp

# Compilation
./mvnw clean compile

# Lancement de l'application
./mvnw spring-boot:run

# Tests unitaires
./mvnw test

# Packaging (génère un JAR exécutable)
./mvnw clean package -DskipTests

# Lancer le JAR packagé
java -jar target/BibliothequeApp-0.0.1-SNAPSHOT.jar

# Forcer la mise à jour des dépendances
./mvnw clean install -U
```

---

## Structure du projet

```
Biblio-Spring/
├── README.md                     # Ce fichier
├── SECURITY-README.md            # Détails de l'architecture sécurité
├── test.sh                       # Script de test de l'API
└── BibliothequeApp/
    ├── pom.xml
    ├── mvnw, mvnw.cmd
    └── src/main/
        ├── java/com/Bibliotheque/BibliothequeApp/
        │   ├── BibliothequeAppApplication.java
        │   ├── config/
        │   │   ├── DataInitializer.java           # Crée admin + user au démarrage
        │   │   └── OpenApiConfig.java             # Config Swagger + Bearer JWT
        │   ├── controller/
        │   │   ├── AuteurController.java
        │   │   └── LivreController.java
        │   ├── security/
        │   │   ├── config/SecurityConfig.java     # Règles HTTP + RBAC
        │   │   ├── jwt/
        │   │   │   ├── JwtTokenProvider.java      # Génération/validation JWT
        │   │   │   ├── JwtAuthenticationFilter.java
        │   │   │   ├── JwtAuthenticationEntryPoint.java   # 401 JSON
        │   │   │   └── JwtAccessDeniedHandler.java        # 403 JSON
        │   │   ├── service/CustomUserDetailsService.java
        │   │   ├── dto/{LoginRequest,RegisterRequest,AuthResponse}.java
        │   │   └── controller/AuthController.java
        │   ├── model/                             # Entités JPA
        │   │   ├── Auteur.java
        │   │   ├── Livre.java
        │   │   └── Utilisateur.java               # Implémente UserDetails
        │   ├── repository/                        # Spring Data JPA
        │   ├── services/                          # Logique métier
        │   └── exception/                         # @ControllerAdvice
        └── resources/
            └── application.properties
```

---

## Sécurité

L'authentification repose sur des **tokens JWT** signés en **HMAC-SHA512**. Le hash des mots de passe est effectué avec **BCrypt** (coût 10 par défaut).

Pour les détails complets (flux JWT, structure des claims, recommandations production), consulte :

**[SECURITY-README.md](./SECURITY-README.md)**

### Bonnes pratiques avant la production

1. **Génère une nouvelle clé JWT** (64 bytes minimum) :
   ```bash
   openssl rand -base64 64
   ```
   Remplace `jwt.secret` dans `application.properties`.

2. **Change les mots de passe** des comptes `admin` et `user` pré-créés.

3. **Active HTTPS** (ne jamais transmettre du JWT en clair sur HTTP).

4. **Active CORS** dans `SecurityConfig` en spécifiant les origines autorisées.

5. **Ne commit jamais** de vrais secrets — utilise des variables d'environnement :
   ```properties
   jwt.secret=${JWT_SECRET}
   spring.datasource.password=${DB_PASSWORD}
   ```

---

## Dépannage

### ❌ `FATAL: password authentication failed for user "postgres"`

Le mot de passe dans `application.properties` ne correspond pas à celui de PostgreSQL.

```bash
# 1. Vérifie que PostgreSQL tourne
pg_isready

# 2. Réinitialise le mot de passe
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'lemin';"

# 3. Mets à jour application.properties avec le même mot de passe
#    spring.datasource.password=lemin

# 4. Relance l'application
```

### ❌ `FATAL: database "bibliotheque" does not exist`

```bash
sudo -u postgres psql -c "CREATE DATABASE bibliotheque;"
```

### ❌ `Connection refused` sur le port 5432

PostgreSQL n'est pas démarré :

```bash
sudo systemctl start postgresql
sudo systemctl status postgresql
pg_isready
```

Si le port 5432 est occupé par un autre processus (ex : Docker), identifie-le :
```bash
ss -tlnp | grep 5432
```
Puis arrête le processus conflictuel.

### ❌ `Port 8081 already in use`

```bash
# Tue le processus utilisant le port
sudo lsof -ti:8081 | xargs kill -9

# OU change le port dans application.properties
# server.port=8082
```

### ❌ `release version 21 not supported` (erreur de compilation)

Ton JDK est plus ancien que la version cible du projet. Vérifie :
```bash
java -version
javac -version
```

Le projet cible **Java 17** (modifiable dans `pom.xml` → `<java.version>`).

### ❌ `401 Unauthorized` sur un endpoint protégé

Token absent, invalide ou expiré. Reconnecte-toi :
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bibliotheque.com","motDePasse":"admin123"}'
```

### ❌ `403 Forbidden` sur POST/PUT/DELETE

Ton compte n'a pas le rôle `ADMIN`. Connecte-toi avec :
- **Email** : `admin@bibliotheque.com`
- **Mot de passe** : `admin123`

### ❌ L'app ne crée pas les tables / erreurs Hibernate

Vérifie que la base `bibliotheque` existe et que l'utilisateur `postgres` a les droits :
```bash
sudo -u postgres psql -c "\l"                          # liste les bases
sudo -u postgres psql -c "GRANT ALL ON DATABASE bibliotheque TO postgres;"
```

---

## Licence

Projet éducatif — Libre de droit.

---

**Auteur** : [LeminEly](https://github.com/LeminEly)
