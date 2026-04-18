# Espaces utilisateurs et pages HireHub

Ce document relie **chaque page** (URL) à **l’espace** (profil) concerné : utile pour la sécurité (Gateway + Spring Security), les menus (`header.html`) et la répartition équipe.

**Référence technique des routes :** `docs/ROUTES_UI.md`  
**Contrôleurs :** `frontend-service/.../UiController.java`, `HomeController.java`

---

## 1. Les cinq « espaces » (vue produit)

| Espace | Qui ? | Ce qu’il voit dans le menu (cible) |
|--------|--------|--------------------------------------|
| **Public / visiteur** | Non connecté | Accueil, offres, login, inscription |
| **Candidat** | Utilisateur avec rôle **CANDIDAT** | Mon espace candidat (dashboard, candidatures, entretiens, profil) |
| **Recruteur (en attente)** | Compte **recruteur** pas encore approuvé par l’admin | Liens espace recruteur **visibles**, pages accessibles, mais **actions bloquées** (bandeau + `hh-recruteur-locked`) |
| **Recruteur (approuvé)** | Recruteur validé par l’admin | Espace recruteur **pleinement utilisable** (offres, pipeline, entretiens, stats) |
| **Administrateur** | Rôle **ADMIN** | Lien **Administration** + pages `/admin/*` |

En **démo UI** sans backend : `?demo=visiteur|candidat|recruteur_pending|recruteur|admin` (voir `DemoUiAdvice.java`).

---

## 2. Page d’accueil et marketing

| URL | Template Thymeleaf | Espace principal | Notes |
|-----|-------------------|------------------|--------|
| `/` | `index.html` | **Public** (tous peuvent l’ouvrir) | Hero, liens vers offres et inscription ; le contenu des boutons change selon la connexion (démo / futur JWT). |

---

## 3. Espace **public** (accessible sans rôle métier)

Ces URLs sont en principe **ouvertes** (ou accessibles après simple navigation). Certaines actions (ex. postuler) exigent d’être **connecté en candidat** une fois l’auth branchée.

| URL | Template | Rôle cible | Service / équipe (rappel) |
|-----|----------|------------|---------------------------|
| `/offres` | `pages/public/offres.html` | Tous | Données : **offre-service** |
| `/offres/{id}` | `pages/public/offre-detail.html` | Tous | **offre-service** |
| `/offres/{id}/postuler` | `pages/public/postuler.html` | **Candidat** connecté (à enforce côté auth) | **candidature-service** |
| `/login` | `pages/public/login.html` | Tous | **auth-service** |
| `/register` | `pages/public/register.html` | Tous (choix du type de compte) | **auth-service** |
| `/register/candidat` | `pages/public/register-candidat.html` | Futur **candidat** | **auth-service** |
| `/register/recruteur` | `pages/public/register-recruteur.html` | Futur **recruteur** | **auth-service** |
| `/demande-recruteur` | — | Redirection → `/register/recruteur` | Compat anciens liens |
| `/mes-candidatures` | — | Redirection → `/candidat/mes-candidatures` | Compat ancien lien |

---

## 4. Espace **candidat** (`/candidat/*`)

Préfixe d’URL : **`/candidat`**. Rôle attendu : **CANDIDAT** (à protéger par Gateway / Security).

| URL | Template | Contenu métier |
|-----|----------|----------------|
| `/candidat/dashboard` | `pages/candidat/dashboard.html` | Vue d’ensemble perso |
| `/candidat/mes-candidatures` | `pages/candidat/mes-candidatures.html` | Suivi des candidatures | **candidature-service** |
| `/candidat/entretiens` | `pages/candidat/entretiens.html` | Entretiens à venir / passés | **entretien-service** |
| `/candidat/profil` | `pages/candidat/profil.html` | Profil + CV | **auth-service** / profil |

---

## 5. Espace **recruteur** (`/recruteur/*`)

Préfixe : **`/recruteur`**. Rôle attendu : **RECRUTEUR**.  
**Important :** même **avant** approbation admin, l’utilisateur peut **ouvrir** ces pages ; le **verrouillage** est assuré par le fragment `fragments/recruteur-lock.html` + `uiRecruteurPending` (et plus tard par les API).

| URL | Template | Contenu métier |
|-----|----------|----------------|
| `/recruteur/dashboard` | `pages/recruteur/dashboard.html` | KPI, raccourcis | **offre-service** / **candidature-service** |
| `/recruteur/offres` | `pages/recruteur/offres-list.html` | Liste / gestion des offres | **offre-service** |
| `/recruteur/offres/nouvelle` | `pages/recruteur/offre-form.html` | Création / publication | **offre-service** |
| `/recruteur/offres/{id}/pipeline` | `pages/recruteur/pipeline.html` | Pipeline candidats pour une offre | **candidature-service** |
| `/recruteur/entretiens` | `pages/recruteur/entretiens.html` | Planification entretiens | **entretien-service** |
| `/recruteur/statistiques` | `pages/recruteur/statistiques.html` | Stats / graphiques | Agrégation (offre + candidature) |

---

## 6. Espace **administrateur** (`/admin/*`)

Préfixe : **`/admin`**. Rôle attendu : **ADMIN** uniquement.

| URL | Template | Contenu métier |
|-----|----------|----------------|
| `/admin/dashboard` | `pages/admin/dashboard.html` | KPI globaux, liens rapides | Agrégation multi-services |
| `/admin/demandes-recruteur` | `pages/admin/demandes-recruteur.html` | **Valider / refuser** comptes recruteurs | **auth-service** + **notification-service** |
| `/admin/utilisateurs` | `pages/admin/utilisateurs.html` | Comptes, rôles | **auth-service** |
| `/admin/logs` | `pages/admin/logs.html` | Journaux (mock → futur audit) | Selon design |

---

## 7. Synthèse « quel espace pour quelle URL ? »

```
/ .............................. Public (+ contenu adapté si connecté)
/offres, /offres/{id} .......... Public
/offres/{id}/postuler ......... Candidat (connecté)
/login, /register* ............. Public
/candidat/* .................... Candidat
/recruteur/* ................... Recruteur (pending = UI lock, approuvé = actions OK)
/admin/* ....................... Admin
```

---

## 8. Alignement avec la répartition équipe (exemple)

| Espace / pages | Périmètre typique |
|----------------|-------------------|
| Public + login + register + **admin** + layout/menus | Frontend + **auth** (Douae) |
| `/offres*`, `/recruteur/offres*` | **offre-service** (Imane) |
| Postuler, `/candidat/mes-candidatures`, **pipeline** | **candidature-service** (Lydivine) |
| Emails / événements liés aux statuts | **notification-service** (Lydivine) |
| `/candidat/entretiens`, `/recruteur/entretiens` | **entretien-service** (Wissal) |

*(Ajustez les noms selon votre tableau officiel.)*

---

*Dernière mise à jour : alignée sur `UiController` et les templates du `frontend-service`.*
