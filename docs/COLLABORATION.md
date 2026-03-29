# Travail collaboratif HireHub

## Règles simples

1. **Une branche par tâche** : `feature/auth-jwt`, `feature/offre-crud`, `fix/gateway-cors`, etc.
2. **Pull requests** vers `main` : au moins une relecture par un autre membre avant merge.
3. **Limiter les fichiers touchés** : préférer un module (dossier) par PR quand c’est possible.
4. **Ne pas committer** de secrets : mots de passe PostgreSQL, clés JWT, comptes Mailtrap → variables d’environnement ou profil local ignoré (`.gitignore` : `application-local.yml`).

## Propriété des modules (à adapter avec les noms du binôme)

| Module | Sujet typique | Dépendances externes |
|--------|---------------|----------------------|
| hirehub-common | DTO partagés, erreurs API | Aucune Spring |
| eureka-server | Annuaire | — |
| config-server | YAML communs | Eureka (optionnel) |
| api-gateway | Routes, JWT, CORS | Eureka |
| auth-service | Login, rôles, JWT | PostgreSQL `hirehub_auth` |
| offre-service | CRUD offres, filtres | PostgreSQL `hirehub_offre` |
| candidature-service | Pipeline, CV, historique | PostgreSQL `hirehub_candidature`, Feign → notification |
| notification-service | Spring Mail | Mailpit / SMTP |
| entretien-service | Entretiens | PostgreSQL `hirehub_entretien`, Feign → notification |
| frontend-service | Thymeleaf, Feign agrégation | Eureka |

Remplir la colonne « responsable » dans le tableau ci-dessous et le commiter une fois réparti :

| Module | Responsable(s) |
|--------|----------------|
| hirehub-common | _à compléter_ |
| eureka-server + config-server | _à compléter_ |
| api-gateway | _à compléter_ |
| auth-service | _à compléter_ |
| offre-service | _à compléter_ |
| candidature-service | _à compléter_ |
| notification-service | _à compléter_ |
| entretien-service | _à compléter_ |
| frontend-service | _à compléter_ |
| Docker / docs | _à compléter_ |

## Conventions de code

- **Packages** : `com.hirehub.<module>` (déjà en place sur les `*Application`).
- **API REST** : préfixe `/api/v1/...` pour les services ; le gateway expose `/api/...`.
- **Logs** : même format (JSON ou ligne structurée) pour faciliter la démo « admin / logs ».

## Communication inter-services

- Préférer **OpenFeign** + DTO dans **hirehub-common** pour les appels synchrones (notification, lecture d’offre, etc.).
- Documenter tout nouveau endpoint dans le README du module ou dans ce fichier.
