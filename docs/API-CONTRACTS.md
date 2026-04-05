# API-CONTRACTS — HireHub (brouillon d’équipe)

À compléter **en priorité** en réunion (P0). Toute personne peut coder en parallèle une fois les lignes ci-dessous figées.

## Authentification

- En-tête : `Authorization: Bearer <access_token>`
- Claims JWT minimaux : `sub` (user id), `email`, `role` (`CANDIDAT` | `RECRUTEUR` | `ADMIN`), `recruteurApprouve` (boolean, pertinent si `RECRUTEUR`)

## auth-service (exemples — à valider)

| Méthode | Chemin | Description |
|---------|--------|-------------|
| POST | `/api/v1/auth/register/candidat` | Corps : nom, email, mot de passe |
| POST | `/api/v1/auth/register/recruteur` | Corps : + raison sociale, SIRET optionnel, téléphone, etc. |
| POST | `/api/v1/auth/login` | email, password → tokens |
| GET | `/api/v1/admin/recruteurs/en-attente` | Liste comptes `RECRUTEUR` avec `recruteurApprouve=false` |
| POST | `/api/v1/admin/recruteurs/{id}/approuver` | → email + `recruteurApprouve=true` |
| POST | `/api/v1/admin/recruteurs/{id}/rejeter` | Corps : motif optionnel → email refus |

## notification-service (événements)

- `RECRUTEUR_APPROUVE` — destinataire : email du recruteur  
- `RECRUTEUR_REJETE` — idem  

(Payload exact : à définir dans `hirehub-common`.)

---

*Les chemins peuvent être préfixés par le Gateway (`/auth/...`) selon votre configuration de routes.*
