# 6.1. Fonctionnalités réalisées par Douae El Assal

**Périmètre personnel :** frontend-service (interface utilisateur), auth-service, verification-service, espace administrateur, **intégration des microservices**, **sécurité des espaces**, **corrections de bugs** et **tests d’intégration** (notamment e-mails et parcours recruteur/candidat/admin).

---

## 6.1.1. Frontend-service et interface utilisateur

J’ai développé le **frontend-service**, point d’accès unique pour les utilisateurs via le port **8086**. Mes réalisations incluent :

### Pages et espaces

- **Pages publiques :** accueil (avec section témoignages candidats/recruteurs), liste des offres, détail offre, login, inscription candidat/recruteur, support, FAQ.
- **Espace candidat :** dashboard, mes candidatures, **mes entretiens** (titre de l’offre, boutons « Voir ma candidature » et « Détail offre »), profil (téléphone, CV par défaut).
- **Espace recruteur :** dashboard (KPI nouvelles candidatures SOUMISE, liens pipeline), gestion offres, pipeline, détail candidature, planification d’entretien, statistiques (Chart.js), entretiens.
- **Espace administrateur :** dashboard global (comptes + activité plateforme), **offres plateforme** (filtres, lecture seule), **détail offre** avec candidatures associées, **entretiens plateforme** (filtres, libellés lisibles), **gestion des utilisateurs** (blocage / déblocage / suppression).

### Navigation, UX et temps réel

- **Navigation par rôle** : menus distincts dans `header.html` (un candidat ne voit pas le menu recruteur, etc.).
- **Bandeau de verrouillage recruteur** (`recruteur-lock.html`) tant que le compte n’est pas approuvé (recruteur en attente de vérification).
- **Synchronisation multi-onglets** : script `live-sync.js` + endpoint `/api/live/version` — rechargement automatique des pages `/candidat/*` et `/recruteur/*` lors d’un changement côté recruteur ou candidat (sans F5 manuel).
- **Postulation** : formulaire avec choix CV par défaut ou nouvel upload, upload lettre de motivation.

### Intégration technique côté frontend

- **Clients HTTP** (RestTemplate) vers la gateway **8089** : `offre-service`, `candidature-service`, `entretien-service`, `event-service`.
- **Service d’enrichissement** `EntretienDisplayEnrichmentService` : résolution du **titre d’offre** et du **nom du recruteur** pour les vues entretiens (candidat, recruteur, admin).
- **Couche admin** : `AdminController`, `AdminPlatformService`, `AdminPlatformStats`, agrégation KPI (offres, candidatures, entretiens).
- **OAuth Google** : connexion via Google avec enrichissement de session JWT.
- **Spring Security** : protection des routes `/admin/**`, `/recruteur/**`, `/candidat/**` par rôle.

### Routes principales

`/`, `/offres`, `/login`, `/register/candidat`, `/register/recruteur`, `/candidat/*`, `/recruteur/*`, `/admin/dashboard`, `/admin/offres`, `/admin/offres/{id}`, `/admin/entretiens`, `/admin/utilisateurs`, `/api/live/version`.

---

## 6.1.2. Auth-service et gestion des comptes

J’ai implémenté le **auth-service** (port **8081**) :

- Inscription candidat et recruteur avec validation des champs.
- Login avec génération **JWT** (claims : email, rôle, `recruiterApproved`).
- Gestion des rôles : **CANDIDAT**, **RECRUTEUR**, **ADMIN**.
- Endpoints liés à la gestion des comptes recruteurs (état d’approbation, synchronisation avec le profil).
- Publication d’événements **RabbitMQ** lors des décisions métier (approbation / refus / actions admin).
- Hash des mots de passe (**BCrypt**).
- Intégration avec le frontend (session, token JWT propagé vers les microservices via la gateway).

---

## 6.1.3. Verification-service

J’ai développé le **verification-service** (port **8090**) pour la **vérification automatique** des documents recruteur (OCR / contrôle SIRET), avec statuts : `PENDING_AUTO_CHECK`, `APPROVED`, `REVIEW_REQUIRED`, `REJECTED`. Ce service alimente le champ `verification_status` du profil recruteur et remplace une validation manuelle bouton par bouton dans l’UI admin.

---

## 6.1.4. Espace administrateur (conception et évolution)

J’ai conçu et fait évoluer l’interface admin vers une **supervision de plateforme en lecture seule** sur le métier recruteur :

| Page | Rôle |
|------|------|
| `/admin/dashboard` | KPI utilisateurs (candidats, recruteurs, admins) + KPI plateforme (offres publiées/fermées/brouillons, candidatures, entretiens planifiés) |
| `/admin/offres` | Liste de **toutes** les offres avec filtres (statut, ville, contrat, recruteur, mot-clé) |
| `/admin/offres/{id}` | Détail offre + liste des candidatures (supervision) |
| `/admin/entretiens` | Tous les entretiens avec filtres ; affichage **titre offre** et **recruteur** (plus les seuls UUID) |
| `/admin/utilisateurs` | Comptes locaux frontend : blocage, déblocage, suppression |

**Choix produit documenté :** la page `/admin/demandes-recruteur` (file manuelle Approuver/Rejeter) a été **retirée** : l’approbation recruteur est portée par **verification-service** ; l’admin se concentre sur la **supervision** et la **gestion des comptes**.

**APIs admin ajoutées ou consommées (intégration microservices) :**

- `GET /api/offres/admin` et `/admin/stats` (offre-service)
- `GET /entretiens/admin` et `/admin/stats` (entretien-service)
- `GET /candidatures/admin/stats` et `/admin/offre/{id}/count` (candidature-service)

---

## 6.1.5. Corrections de bugs, intégration et tests (responsabilité Douae El Assal)

J’ai pris en charge la **correction des erreurs d’intégration** entre microservices et les **bugs bloquants** sur les parcours livrés :

### Intégration microservices

- Appels frontend → **API Gateway (8089)** avec JWT de session ; gestion des erreurs HTTP et messages utilisateur (flash Thymeleaf).
- Fermeture d’offre : enchaînement **offre-service** + rejet automatique des candidatures `SOUMISE` (**candidature-service**) + notifications.
- Planification entretien : chaîne **frontend → entretien-service → candidature-service** (statut `ENTRETIEN`) + **email-service** via RabbitMQ.
- **Suppression des mocks** côté candidature-service (profils mock/sandbox) pour garantir des données **PostgreSQL réelles** en démo et en tests.

### Bugs corrigés (exemples)

| Problème | Correction |
|----------|------------|
| Planification entretien : `Could not convert String to UUID` (Hibernate) | Type `UUID` pour l’id entretien en base ; alignement entité JPA / PostgreSQL |
| Page candidature blanche après erreur entretien | Template `candidature-detail.html` : ne plus masquer tout le contenu si `error` flash |
| KPI admin toujours à **0** | Parsing correct des stats (`Integer` → `long`) + repli sur listes admin ; auth allégée sur stats candidatures |
| E-mail entretien sans nom d’offre | Feign **offre-service** dans entretien-service → `offerTitle` réel dans le payload RabbitMQ / template HTML |
| Affichage UUID dans « Mes entretiens » et admin | `EntretienDisplayEnrichmentService` + colonnes titre offre / libellé recruteur |

### Sécurité

- Règles Spring Security par rôle sur les espaces UI.
- Endpoints admin candidatures : accès pipeline pour **ADMIN** en lecture (supervision).
- Téléchargements CV / lettre : contrôles d’accès côté candidature-service (candidat propriétaire, recruteur propriétaire de l’offre).

### Tests et outillage

- Script **`scripts/test-all-mailpit-notifications.ps1`** : publication des événements RabbitMQ (`EmailEventDTO` + `__TypeId__`) pour valider les types de mails dans **Mailpit** (port 8025).
- Scénarios manuels documentés : fermeture offre, dashboard recruteur, planification entretien, espace admin.
- Mise à jour documentation : `docs/ESPACES_ET_PAGES.md`, `docs/ROUTES_UI.md`, diagrammes séquence rapport §3.5.

### Notifications e-mail (contribution intégration)

- Alignement **entretien-service** → **email-service** sur `EmailEventDTO` (planification / annulation entretien).
- Vérification que le template « Entretien programmé » affiche bien le **titre du poste**, la date, le lieu ou le lien visio.

---

## 6.1.6. Synthèse du rôle dans l’équipe

| Domaine | Contribution Douae El Assal |
|---------|---------------------------|
| UI / UX | Frontend complet, admin supervision, live-sync, accueil témoignages |
| Auth & comptes | auth-service, rôles, JWT, OAuth |
| Conformité recruteur | verification-service (OCR / SIRET) |
| Intégration | Gateway, clients REST, enchaînements offre–candidature–entretien–email |
| Qualité | Bugs critiques, suppression mocks, tests Mailpit, doc routes et séquences |

Les services **offre-service** (Imane), **candidature-service** et **email-service** (Lydivine) et **entretien-service** (Wissal) ont été **intégrés et débogués** depuis le frontend et les flux transverses ; les corrections listées en §6.1.5 assurent la **cohérence bout-en-bout** de la livraison.
