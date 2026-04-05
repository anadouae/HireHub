# HireHub — Description complète du projet (équipe de 4)

Document de référence pour le binôme étendu : **4 développeurs**, base **PostgreSQL 17**, architecture **microservices Spring Cloud**.

---

## 1. Vision produit

**HireHub** est une plateforme **ATS** (Applicant Tracking System) : gestion bout en bout du recrutement — publication d’offres, candidatures avec CV (PDF), pipeline des statuts, entretiens, notifications par email, supervision par les administrateurs.

### Rôles

| Rôle | Capacités |
|------|-----------|
| **Candidat** | Consulter les offres, postuler, suivre ses candidatures et entretiens, recevoir des emails. |
| **Recruteur** | Gérer les offres, le pipeline, télécharger les CV, planifier des entretiens, statistiques. |
| **Admin** | Tout le périmètre recruteur + **valider ou refuser les comptes recruteurs**, gérer les utilisateurs (suspension, rôles), dashboard global, journaux. |

### Règle métier clé : inscription recruteur

- À l’**inscription**, l’utilisateur choisit **Candidat** ou **Recruteur** (formulaires **différents**).
- Un compte **Recruteur** peut **se connecter** et **ouvrir son espace** (`/recruteur/*`) **immédiatement**.
- **Toutes les fonctionnalités métier** (créer une offre, pipeline, etc.) restent **verrouillées** tant que l’**admin** n’a pas **approuvé** le compte (`recruteurApprouve = true` côté `auth-service`).
- Après **approbation** : déverrouillage + **email** au recruteur.  
- Après **refus** : compte toujours connectable mais toujours verrouillé (ou politique équipe) + **email de refus** — à trancher en équipe ; minimum : email informant le refus.

Les **admins** ne s’inscrivent pas en ligne : comptes créés manuellement ou seed.

---

## 2. Stack technique

| Couche | Technologies |
|--------|----------------|
| Langage | Java **17** |
| Framework | **Spring Boot 3.2**, **Spring Cloud 2023** (Eureka, Gateway, Config, OpenFeign) |
| Sécurité | Spring Security, **JWT** |
| Données | **Spring Data JPA**, **PostgreSQL 17** (une base logique par service concerné) |
| Emails | Spring Mail, **Mailpit** (dev) / SMTP prod |
| UI | **Thymeleaf**, Bootstrap 5, Chart.js |
| Build / exécution | **Maven** (multi-modules), **Docker Compose** |
| Tests | JUnit 5, Mockito |

**Ports habituels**

| Service | Port |
|---------|------|
| eureka-server | 8761 |
| config-server | 8888 |
| api-gateway | 8080 |
| auth-service | 8081 |
| offre-service | 8082 |
| candidature-service | 8083 |
| notification-service | 8084 |
| entretien-service | 8085 |
| frontend-service | 8086 |

---

## 3. Microservices et responsabilités

| Service | Rôle |
|---------|------|
| **eureka-server** | Annuaire des instances. |
| **config-server** | Configuration centralisée (fichiers YAML). |
| **api-gateway** | Point d’entrée, routage, CORS, **filtre JWT**. |
| **auth-service** | Inscription (candidat / recruteur), login, JWT, rôles, **flag approbation recruteur**, endpoints admin (liste users, approuver / rejeter recruteur). |
| **offre-service** | CRUD offres, filtres, pagination, stats par offre. |
| **candidature-service** | Candidatures, upload CV, workflow de statuts, historique, appels Feign vers notification. |
| **notification-service** | Envoi d’emails (templates Thymeleaf ou texte). |
| **entretien-service** | Entretiens, notifications associées. |
| **frontend-service** | Pages Thymeleaf, agrégation via **Feign**, **pas de BDD dédiée**. |

---

## 4. Contrat d’équipe pour travailler en parallèle

Pour limiter les **dépendances bloquantes** :

1. **Semaine 1 — `docs/API-CONTRACTS.md`** (session commune, ~2 h)  
   URLs REST, corps JSON, en-tête `Authorization: Bearer`, codes d’erreur, DTO dans `hirehub-common`.

2. **Stubs** : si un service n’est pas prêt, **Feign** pointe vers un **mock** ou des réponses statiques dans le frontend jusqu’au merge.

3. **Branches Git** : `feat/<service>` ou `feat/<prenom>-<module>`, merge sur `main` après revue.

4. **PostgreSQL** : un script d’init (ou Flyway) par service, documenté dans le README.

---

## 5. Répartition des tâches — 4 personnes (parallèle maximal)

Les blocs sont calés pour que **chacun possède un module Maven** principal ; les croisements se font sur le **contrat API** et les **DTO** déjà figés.

### Personne **A** — Fondations & notifications

| Tâche | Détail |
|-------|--------|
| Parent Maven + `hirehub-common` | Structure modules, DTO partagés, enums (statuts candidature, **statut approbation recruteur**). |
| **eureka-server** + **config-server** | Démarrage, fichiers dans `config-repo`. |
| **notification-service** | Spring Mail, templates email (bienvenue, changement statut, **approbation / refus recruteur**). |
| **Docker Compose** | PostgreSQL 17 + Mailpit ; script init des bases (aligné sur le projet). |

**Dépendance** : aucune sur B/C/D si les **événements** (payload email) sont décrits dans `API-CONTRACTS.md`.

---

### Personne **B** — Gateway & frontend

| Tâche | Détail |
|-------|--------|
| **api-gateway** | Routes `lb://…`, filtre JWT, règles `/recruteur/**`, `/admin/**`. |
| **frontend-service** | Thymeleaf : inscription **2 formulaires** (candidat / recruteur), login, layout, **verrouillage UI** espace recruteur si non approuvé, pages publiques + espaces par rôle. |
| **Feign** | Clients vers auth, offre, candidature, etc. ; gestion token (interceptor). |

**Dépendance** : contrats API + champs JWT (claims : `role`, `recruteurApprouve`). En attendant : **données mock** et param `?demo=` pour les états UI.

---

### Personne **C** — Auth & candidatures

| Tâche | Détail |
|-------|--------|
| **auth-service** | Inscription candidat vs recruteur (champs différents), hash mot de passe, JWT, rôles, **champs `recruteurApprouve` (bool)**, endpoints **admin** : liste recruteurs en attente, `POST .../approuver`, `POST .../rejeter` + appel notification. |
| **candidature-service** | Candidatures, PDF, historique, Feign → notification. |

**Dépendance** : DTO dans `hirehub-common` ; **notification** peut être mockée au début (log only).

---

### Personne **D** — Offres & entretiens

| Tâche | Détail |
|-------|--------|
| **offre-service** | CRUD, JPA Specifications / filtres, pagination, stats. |
| **entretien-service** | CRUD entretiens, liens candidature/offre, Feign → notification. |

**Dépendance** : identifiants utilisateur / offre dans le contrat ; pas besoin d’attendre le frontend pour tester via Postman.

---

## 6. Phases de livraison (jalons)

| Phase | Contenu | Qui pilote |
|-------|---------|------------|
| **P0** | `API-CONTRACTS.md` + `hirehub-common` + Docker PG 17 | A + équipe |
| **P1** | Eureka, Config, Gateway minimal | A + B |
| **P2** | Auth (inscription 2 types + JWT + approbation admin) + emails | C + A |
| **P3** | Offre-service + pages offres (Feign) | D + B |
| **P4** | Candidature + notifications statut | C + A |
| **P5** | Entretien + UI recruteur complète | D + B |
| **P6** | Admin dashboard, seed data, README final | Équipe |

---

## 7. Fichiers utiles dans le dépôt

- `docs/ARCHITECTURE.md` — schéma technique  
- `docs/ROUTES_UI.md` — routes Thymeleaf  
- `docs/Repartition_taches_HireHub.md` / `.csv` — variantes plus courtes  
- `README.md` — prérequis, `mvnw`, `docker compose`  

---

## 8. À créer ensemble (recommandé)

- **`docs/API-CONTRACTS.md`** — liste exhaustive des endpoints et DTO (priorité absolue pour le parallélisme).

---

*Document aligné sur le dépôt HireHub — PostgreSQL 17, inscription candidat/recruteur, espace recruteur verrouillé jusqu’à approbation admin.*
