# Sécurité — BibliothèqueApp

## Architecture de Sécurité

Ce document décrit l'implémentation complète de la sécurité mise en place pour l'application **BibliothèqueApp**. L'architecture repose sur **Spring Security 7.x** avec **authentification par JWT (JSON Web Token)** et un système de **rôles (RBAC)**.

---

## Stack Technique

| Technologie | Version | Rôle |
|---|---|---|
| Spring Security | 7.x (Spring Boot 4.0.5) | Framework d'authentification et d'autorisation |
| JJWT (io.jsonwebtoken) | 0.12.6 | Création et validation des tokens JWT |
| BCrypt | Intégré à Spring Security | Hachage des mots de passe |
| SpringDoc OpenAPI | 3.0.3 | Documentation Swagger avec support JWT |

---

## Structure du Package Security

```
src/main/java/com/Bibliotheque/BibliothequeApp/security/
├── config/
│   └── SecurityConfig.java          # Configuration principale Spring Security
├── jwt/
│   ├── JwtTokenProvider.java        # Génération et validation des tokens JWT
│   ├── JwtAuthenticationFilter.java # Filtre OncePerRequestFilter pour l'auth JWT
│   └── JwtAuthenticationEntryPoint.java # Gestion des erreurs 401
├── service/
│   └── CustomUserDetailsService.java # Chargement des utilisateurs depuis la DB
├── dto/
│   ├── LoginRequest.java            # DTO pour la connexion
│   ├── RegisterRequest.java         # DTO pour l'inscription
│   └── AuthResponse.java            # DTO pour la réponse authentification
└── controller/
    └── AuthController.java          # Endpoints /api/auth/**
```

---

## Flux d'Authentification

### 1. Inscription (`POST /api/auth/register`)

```
Client → [POST /api/auth/register] → AuthController.register()
  → Validation champs (email unique, nom unique)
  → Création Utilisateur avec mot de passe hashé (BCrypt)
  → Rôle par défaut : "USER"
  → Génération du token JWT
  → Réponse : { token, email, role }
```

### 2. Connexion (`POST /api/auth/login`)

```
Client → [POST /api/auth/login] → AuthController.login()
  → AuthenticationManager.authenticate()
  → CustomUserDetailsService.loadUserByUsername()
  → Vérification du mot de passe (BCrypt)
  → Génération du token JWT
  → Réponse : { token, email, role }
```

### 3. Requêtes Authentifiées

```
Client → [Requête avec Header: Authorization: Bearer <token>]
  → JwtAuthenticationFilter.doFilterInternal()
  → Validation du token (signature + expiration)
  → Extraction de l'email depuis le token
  → Chargement de l'utilisateur (CustomUserDetailsService)
  → Création du UsernamePasswordAuthenticationToken
  → Mise à jour du SecurityContextHolder
  → Accès à la ressource
```

---

## Configuration Détaillée

### SecurityConfig (`security/config/SecurityConfig.java`)

| Aspect | Configuration |
|---|---|
| CSRF | Désactivé (API REST stateless) |
| CORS | Désactivé (à configurer selon l'environnement) |
| Session | STATELESS (pas de session HTTP) |
| Gestion des exceptions | JwtAuthenticationEntryPoint (401 JSON) |

**Règles d'autorisation :**

| Méthode | Endpoint | Rôle requis |
|---|---|---|
| `POST` | `/api/auth/**` | PUBLIC (permitAll) |
| `GET` | `/swagger-ui/**`, `/v3/api-docs/**` | PUBLIC (permitAll) |
| `GET` | `/livres`, `/auteurs` | Authentifié (tout rôle) |
| `GET` | `/livres/{id}`, `/auteurs/{id}` | Authentifié (tout rôle) |
| `POST` | `/livres`, `/auteurs` | ADMIN |
| `PUT` | `/livres/{id}`, `/auteurs/{id}` | ADMIN |
| `DELETE` | `/livres/{id}`, `/auteurs/{id}` | ADMIN |

### JwtTokenProvider (`security/jwt/JwtTokenProvider.java`)

- **Algorithme de signature** : HMAC-SHA256
- **Clé secrète** : Base64 encodée, définie dans `application.properties` (`jwt.secret`)
- **Expiration** : 24h (configurable via `jwt.expiration` en millisecondes)
- **Claims** : `sub` (email), `iat` (date d'émission), `exp` (date d'expiration)

### JwtAuthenticationFilter (`security/jwt/JwtAuthenticationFilter.java`)

- Étend `OncePerRequestFilter` (garantit une exécution unique par requête)
- Extrait le token du header `Authorization: Bearer <token>`
- Valide le token et configure le contexte de sécurité
- Passe au filtre suivant si pas de token présent

### JwtAuthenticationEntryPoint (`security/jwt/JwtAuthenticationEntryPoint.java`)

- Retourne une réponse JSON structurée pour les erreurs 401 :

```json
{
  "status": 401,
  "message": "Accès non autorisé : token manquant ou invalide",
  "timestamp": "2026-06-05T12:00:00"
}
```

### CustomUserDetailsService (`security/service/CustomUserDetailsService.java`)

- Implémente `UserDetailsService`
- Charge l'utilisateur par email depuis `UtilisateurRepository`
- Retourne une instance de `Utilisateur` qui implémente `UserDetails`

---

## Modèle Utilisateur

### Entité JPA : `Utilisateur` (`model/Utilisateur.java`)

| Champ | Type | Contrainte | Description |
|---|---|---|---|
| `id` | `Long` | PK, auto-généré | Identifiant unique |
| `nom` | `String` | UNIQUE, NOT NULL | Nom d'utilisateur |
| `email` | `String` | UNIQUE, NOT NULL | Email (utilisé comme login) |
| `motDePasse` | `String` | NOT NULL | Mot de passe (hashé BCrypt) |
| `role` | `String` | NOT NULL | Rôle : `USER` ou `ADMIN` |

L'entité implémente `UserDetails` de Spring Security :
- `getAuthorities()` → `ROLE_USER` ou `ROLE_ADMIN`
- `getUsername()` → retourne l'email
- `getPassword()` → retourne le mot de passe hashé
- Comptes non-expirés, non-verrouillés, activés par défaut

---

## Comptes Pré-initialisés

Un `DataInitializer` (`config/DataInitializer.java`) crée automatiquement deux comptes au démarrage :

| Email | Mot de passe | Rôle |
|---|---|---|
| `admin@bibliotheque.com` | `admin123` | ADMIN (accès complet) |
| `user@bibliotheque.com` | `user123` | USER (lecture seule) |

---

## Utilisation de l'API

### Obtenir un token

```bash
# Connexion
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bibliotheque.com","motDePasse":"admin123"}'

# Réponse
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "admin@bibliotheque.com",
  "role": "ADMIN"
}
```

### Utiliser le token

```bash
# Liste des livres (authentifié)
curl http://localhost:8081/livres \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

# Créer un livre (admin seulement)
curl -X POST http://localhost:8081/livres \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  -H "Content-Type: application/json" \
  -d '{"titre":"1984","isbn":"978-0451524935","anneePublication":1949,"auteur":{"id":1}}'
```

---

## Swagger / OpenAPI

L'interface Swagger est accessible après authentification :

| Ressource | URL |
|---|---|
| Swagger UI | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) |
| Docs OpenAPI | [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs) |

**Configuration OpenAPI** (`config/OpenApiConfig.java`) :
- Titre : "API Bibliothèque"
- Version : "1.0"
- Schéma de sécurité : Bearer JWT (bouton "Authorize" dans Swagger UI)

Pour tester les endpoints protégés dans Swagger UI :
1. Cliquez sur le bouton **Authorize**
2. Entrez votre token JWT : `Bearer <votre-token>`
3. Les requêtes seront automatiquement authentifiées

---

## Recommandations Production

Avant de déployer en production :

1. **Générer une nouvelle clé JWT** :
   ```bash
   openssl rand -base64 64
   ```
   Remplacer `jwt.secret` dans `application.properties`

2. **Activer CORS** : Configurer les origines autorisées dans `SecurityConfig`

3. **HTTPS** : Forcer TLS en production

4. **Rotation des clés** : Implémenter un mécanisme de rotation pour les clés JWT

5. **Rate Limiting** : Ajouter un rate limiter sur `/api/auth/**`

6. **Logs** : Activer les logs de sécurité pour l'audit :
   ```properties
   logging.level.org.springframework.security=DEBUG
   ```

---

## Tests des Endpoints

```bash
# Test rapide de l'API avec un token
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bibliotheque.com","motDePasse":"admin123"}' | \
  python3 -c "import sys,json;print(json.load(sys.stdin)['token'])")

# Vérifier que le token fonctionne
curl -s -H "Authorization: Bearer $TOKEN" http://localhost:8081/livres | python3 -m json.tool
```
