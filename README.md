# 📚 Système de Gestion Scolaire — API REST (Spring Boot)

Projet pédagogique **L3 Informatique — IPD Dakar** (PDWA-L3, 2025–2026).

Back-end d'une plateforme de gestion des **étudiants**, **enseignants**, **cours** et **inscriptions**, sécurisée par **JWT + Refresh Token** (authentification stateless, contrôle d'accès par rôles).

---

## 🚀 Démarrage rapide

### Prérequis

- Java **17+** (recommandé : 21)
- Maven (ou `./mvnw`)
- PostgreSQL **14+** (ou lancez les tests avec H2 embarqué)

### 1. Créer la base PostgreSQL

```sql
CREATE DATABASE gestion_scolaire;
```

### 2. Variables d'environnement (optionnel, valeurs par défaut incluses)

| Variable                    | Défaut                                              | Description                      |
| --------------------------- | --------------------------------------------------- | -------------------------------- |
| `DB_URL`                    | `jdbc:postgresql://localhost:5432/gestion_scolaire` | URL JDBC                         |
| `DB_USERNAME`               | `postgres`                                          | Utilisateur PostgreSQL           |
| `DB_PASSWORD`               | `postgres`                                          | Mot de passe                     |
| `JWT_SECRET`                | (secret de dev)                                     | Clé HMAC ≥ 32 octets             |
| `JWT_EXPIRATION_MS`         | `900000` (15 min)                                   | Durée de vie access token        |
| `JWT_REFRESH_EXPIRATION_MS` | `604800000` (7 j)                                   | Durée de vie refresh token       |
| `UPLOAD_DIR`                | `uploads`                                           | Dossier de stockage des fichiers |

### 3. Lancer l'application

```bash
./mvnw spring-boot:run
```

Au premier démarrage, le `DataInitializer` crée les rôles et des **données de démonstration** :

| Compte                          | Mot de passe  | Rôle       |
| ------------------------------- | ------------- | ---------- |
| `admin`                         | `admin123`    | ADMIN      |
| `ens-001` … `ens-003`           | `password123` | ENSEIGNANT |
| `etu-2025-001` … `etu-2025-005` | `password123` | ETUDIANT   |

### 4. Documentation interactive

- Swagger UI : <http://localhost:8080/swagger-ui.html>
- Spec OpenAPI : <http://localhost:8080/openapi.yaml>

---

## 🔐 Authentification (JWT + Refresh Token)

1. **Inscription** — `POST /auth/register` (rôle `ETUDIANT` par défaut) ou **Connexion** — `POST /auth/login`.
2. La réponse contient `accessToken` (**15 min**) et `refreshToken` (**7 j**, stocké en base, jetable).
3. Appels protégés : en-tête `Authorization: Bearer <accessToken>`.
4. Quand l'access token expire :
   ```bash
   POST /auth/refresh
   { "refreshToken": "<refreshToken>" }
   ```
   → Nouveau couple (`accessToken` + `refreshToken`). **Rotation** : l'ancien refresh token est révoqué à chaque renouvellement.
5. Déconnexion : `POST /auth/logout` révoque le refresh token.

### Exemple de flux

```bash
# Connexion
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Accès protégé
curl http://localhost:8080/etudiants?page=0&size=5 \
  -H "Authorization: Bearer <accessToken>"
```

---

## 📦 Endpoints principaux

| Méthode | URL                     | Rôles                     | Description                        |
| ------- | ----------------------- | ------------------------- | ---------------------------------- |
| POST    | `/auth/register`        | Public                    | Inscription (ETUDIANT)             |
| POST    | `/auth/login`           | Public                    | Connexion → JWT + refresh          |
| POST    | `/auth/refresh`         | Public                    | Renouvellement (rotation)          |
| POST    | `/auth/logout`          | Auth                      | Révocation du refresh token        |
| GET     | `/etudiants`            | ADMIN, ENSEIGNANT         | Liste **paginée** + `?search=`     |
| POST    | `/etudiants`            | ADMIN                     | Créer un étudiant                  |
| PUT     | `/etudiants/{id}`       | ADMIN                     | Modifier un étudiant               |
| DELETE  | `/etudiants/{id}`       | ADMIN                     | Supprimer un étudiant              |
| POST    | `/etudiants/{id}/photo` | ADMIN, ETU (propriétaire) | Upload photo ≤ **2 Mo** (JPEG/PNG) |
| POST    | `/etudiants/{id}/docs`  | ADMIN, ETU (propriétaire) | Upload PDF ≤ 5 Mo                  |
| GET     | `/cours`                | Tous authentifiés         | Liste des cours                    |
| POST    | `/inscriptions`         | ADMIN                     | Inscrire un étudiant à un cours    |
| GET     | `/inscriptions`         | ADMIN, ENSEIGNANT         | Liste des inscriptions             |
| GET     | `/admin/users`          | ADMIN                     | Gestion des utilisateurs           |

> Paramètres de pagination sur les listes : `?page=0&size=10&sort=lastName,asc`

---

## 🧪 Tester (sans PostgreSQL)

```bash
./mvnw test
```

Les tests utilisent **H2** (mode PostgreSQL) et vérifient notamment la **rotation du refresh token**.

---

## 🗂️ Structure du projet (architecture en couches)

```
src/main/java/sn/ipd/gestion_scolaire/
├── config/        → SecurityConfig, WebConfig, DataInitializer
├── controller/    → Couche REST (Auth, Etudiant, Enseignant, Cours, Inscription, Admin)
├── dto/           → Records séparés des entités (+ validation @Valid)
├── entity/        → Entités JPA (User, Role, Etudiant, Enseignant, Cours, Inscription, RefreshToken)
├── exception/     → GlobalExceptionHandler (@RestControllerAdvice) + ApiError
├── repository/    → Interfaces Spring Data JPA
├── security/      → JwtService, JwtAuthenticationFilter, RefreshTokenService, UserDetailsService
└── service/       → Logique métier
```

---

## ⚙️ Fonctionnalités évaluées couvertes

- ✅ **Spring Security / JWT (25 pts)** : inscription, connexion, filtre JWT, rôles, refresh token avec rotation
- ✅ **CRUD & JPA (25 pts)** : entités + relations, pagination, DTOs, validation `@Valid`
- ✅ **Exception globale (20 pts)** : `@RestControllerAdvice`, réponse uniforme `ApiError`, codes HTTP corrects (400/401/403/404/409/413/500)
- ✅ **Upload fichiers (15 pts)** : photo + PDF, validation **MIME/signature magique**, contrôle de taille, stockage local
- ✅ **Documentation & code (10 pts)** : Swagger UI, OpenAPI 3, structure en couches, README
- ✅ **Script SQL** : `db/init.sql` (5 étudiants, 3 cours)

---

## 📄 Livrables du projet

- Code source + historique Git
- Rapport technique (architecture, choix techniques, difficultés)
- Swagger UI fonctionnel + collection Postman exportable
- `db/init.sql` (données de test)
- `README.md`

## 📚 Références

Spring in Action (Manning), Baeldung (JWT, File Upload, Error Handling), OpenAPI Spec v3.1, Hibernate ORM 6 — voir cahier des charges.
