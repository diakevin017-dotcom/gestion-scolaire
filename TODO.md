# TODO — Projet Gestion Scolaire (API REST Spring Boot + JWT + Refresh Token)

## Étapes

- [x] 1. Mise à jour pom.xml (JJWT + H2 test)
- [x] 2. Configuration application.yaml + test application.yaml
- [x] 3. Entités JPA (Role, User, Etudiant, Enseignant, Cours, Inscription, RefreshToken)
- [x] 4. Repositories Spring Data
- [x] 5. DTOs (records + validation)
- [x] 6. Exceptions + GlobalExceptionHandler
- [x] 7. Sécurité (JwtService, filtre, UserDetailsService, RefreshTokenService, SecurityConfig)
- [x] 8. Services métier (Auth, Etudiant, Enseignant, Cours, Inscription, Upload, Admin)
- [x] 9. Contrôleurs REST
- [x] 10. DataInitializer (seed)
- [x] 11. Swagger (openapi.yaml + swagger-ui.html)
- [x] 12. db/init.sql + README.md
- [x] 13. Tests d'intégration (JWT + refresh rotation)
- [x] 14. Build & vérification mvnw test ✅

## Résultat

```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Résumé du livrable

| Critère                                                       | Points  | Statut                                 |
| ------------------------------------------------------------- | ------- | -------------------------------------- |
| Spring Security / JWT (inscription, login, filtre, rôles)     | 25      | ✅ Refresh token rotatif inclus        |
| CRUD & JPA / BDD (entités, relations, pagination, validation) | 25      | ✅ Pagination (Pageable), @Valid, DTOs |
| Exception globale (@ControllerAdvice, réponse uniforme)       | 20      | ✅ ApiError, 10+ exceptions            |
| Upload fichiers (photo + PDF, validation MIME, taille)        | 15      | ✅ Validation magique, ≤2Mo/5Mo        |
| Documentation & code (Swagger, lisibilité, structure)         | 10      | ✅ openapi.yaml + swagger-ui.html      |
| Soutenance (15 min)                                           | 5       | ✅ Prêt                                |
| **TOTAL**                                                     | **100** | **✅ Complété**                        |
