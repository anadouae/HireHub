# Répartition du travail par microservice — HireHub

Chaque ligne correspond à **un module Maven** (ou équivalent infra). Les **personnes** peuvent se répartir les services ; l’important est qu’**un propriétaire principal** par service évite les conflits Git sur le même dossier.

| Microservice / module | Port (habituel) | Base PostgreSQL | Rôle & périmètre |
|------------------------|-----------------|-----------------|------------------|
| **hirehub-parent** | — | — | POM parent : versions Spring Boot / Cloud, liste des modules, plugins Maven communs. |
| **hirehub-common** | — | — | **DTO**, enums (statuts candidature, approbation recruteur), erreurs API partagées ; **aucune** exécution runtime seule. Toute évolution de contrat passe ici en premier. |
| **eureka-server** | 8761 | — | Annuaire des instances ; enregistrement des microservices. |
| **config-server** | 8888 | — | Configuration centralisée (fichiers YAML du config-repo) ; profils `dev` / `docker`. |
| **api-gateway** | 8080 | — | Point d’entrée unique : routage `lb://…`, CORS, **filtre JWT**, règles d’accès `/recruteur/**`, `/admin/**`, agrégation optionnelle. |
| **auth-service** | 8081 | `hirehub_auth` | Inscription **candidat** / **recruteur**, login, JWT, rôles, champ **recruteur approuvé**, endpoints **admin** (liste en attente, approuver / rejeter), appels vers **notification-service** pour les emails de décision. |
| **offre-service** | 8082 | `hirehub_offre` | CRUD offres, pagination, filtres (ville, contrat, etc.), visibilité publique vs recruteur ; **refuser les écritures** si recruteur non approuvé (règle métier alignée sur le JWT). |
| **candidature-service** | 8083 | `hirehub_candidature` | Candidatures, upload **CV PDF**, pipeline / statuts, historique ; **Feign → notification-service** ; vérif recruteur approuvé pour actions côté RH si applicable. |
| **notification-service** | 8084 | — (pas de persistance métier obligatoire) | **Spring Mail** : templates (bienvenue, changement statut, entretien, **approbation / refus recruteur**). Consommé par auth, candidature, entretien. |
| **entretien-service** | 8085 | `hirehub_entretien` | Planification entretiens, créneaux, liens / lieu ; **Feign → notification-service** ; règles d’accès recruteur approuvé. |
| **frontend-service** | 8086 | — | **Thymeleaf** : pages publiques, candidat, recruteur (dont **lock UI** si pending), admin ; **Feign** vers les APIs ; pas de BDD dédiée. |

---

## Dépendances entre services (ordre d’intégration logique)

```text
hirehub-common  ←── tous les services Java

eureka-server   ←── tous les services (client Eureka)
config-server   ←── tous les services (client Config)

api-gateway     ←── routage vers auth, offre, candidature, entretien, notification (selon design)

auth-service    ──→ notification-service (emails approbation / refus recruteur)
candidature-service ──→ notification-service
entretien-service   ──→ notification-service

frontend-service ──→ gateway (ou services directement en dev) via Feign / RestClient
```

---

## Infra hors module Java

| Élément | Responsabilité typique |
|---------|-------------------------|
| **Docker Compose** | PostgreSQL **17**, **Mailpit**, réseaux, variables d’environnement ; scripts `docker/postgres/init/*.sql` par base. |
| **Documentation** | `API-CONTRACTS.md`, `ROUTES_UI.md`, `PROJET_HIREHUB_COMPLET.md`. |

---

## Répartition équipe de 4 (exemple de mapping 1 service / personne)

| Personne | Microservices principaux |
|----------|---------------------------|
| **A** | `eureka-server`, `config-server`, `notification-service`, Docker + init BDD, évolutions `hirehub-common` (avec accord équipe). |
| **B** | `api-gateway`, `frontend-service`. |
| **C** | `auth-service`. |
| **D** | `offre-service`, `candidature-service`, `entretien-service`. |

Le **parent** et **common** se gèrent en **PR courtes** validées par toute l’équipe dès qu’un contrat change.

---

*Voir aussi `docs/Repartition_taches_HireHub.md` et `docs/PROJET_HIREHUB_COMPLET.md` pour le détail pédagogique et les phases.*
