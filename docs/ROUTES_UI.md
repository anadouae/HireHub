# Routes UI HireHub (frontend-service)

Pages **Thymeleaf** ; données mock ou Feign selon l’avancement. Référence produit : `docs/PROJET_HIREHUB_COMPLET.md`.

## Public

| URL | Page |
|-----|------|
| `/` | Accueil |
| `/offres` | Liste des offres + filtres |
| `/offres/{id}` | Détail offre |
| `/offres/{id}/postuler` | Formulaire candidature |
| `/login` | Connexion |
| `/register` | Hub inscription (liens candidat / recruteur) |
| `/register/candidat` | Inscription **candidat** |
| `/register/recruteur` | Inscription **recruteur** (champs entreprise, etc.) |
| `/demande-recruteur` | **Redirection** vers `/register/recruteur` (anciens liens) |
| `/mes-candidatures` | Redirige vers `/candidat/mes-candidatures` |

## Candidat

| URL | Page |
|-----|------|
| `/candidat/dashboard` | Tableau de bord |
| `/candidat/mes-candidatures` | Candidatures + timeline |
| `/candidat/entretiens` | Liste des entretiens |
| `/candidat/profil` | Profil (lien vers inscription recruteur si besoin d’un 2ᵉ compte) |

## Recruteur

Toutes les pages incluent le bandeau **en attente de validation** et la classe `hh-recruteur-locked` si `uiRecruteurPending` (JWT / démo).

| URL | Page |
|-----|------|
| `/recruteur/dashboard` | KPI + candidatures récentes |
| `/recruteur/offres` | Liste des offres |
| `/recruteur/offres/nouvelle` | Création d’offre |
| `/recruteur/offres/{id}/pipeline` | Pipeline / statuts |
| `/recruteur/entretiens` | Planification entretiens |
| `/recruteur/statistiques` | Graphiques (Chart.js) |

## Admin

| URL | Page |
|-----|------|
| `/admin/dashboard` | KPI globaux + graphique |
| `/admin/demandes-recruteur` | **Validation** des comptes recruteurs inscrits (approuver / rejeter + emails) |
| `/admin/utilisateurs` | Gestion des comptes |
| `/admin/logs` | Journaux (mock) |

## Démo UI (menus conditionnels)

Paramètre `?demo=` sur l’URL :

`visiteur` | `candidat` | `recruteur_pending` | `recruteur` | `admin`

Exemple : `http://localhost:8086/?demo=recruteur_pending` puis naviguer vers `/recruteur/dashboard` pour voir le verrouillage.

Voir `DemoUiAdvice.java` pour le détail des attributs de modèle (`uiMenuRecruteur`, `uiRecruteurPending`, etc.).
