# Parcours recruteur — règles métier HireHub (inscription + validation admin)

## Principe (version actuelle)

1. À l’**inscription**, l’utilisateur choisit **Candidat** ou **Recruteur** (`/register` → formulaires distincts).
2. Un compte **Recruteur** reçoit le rôle recruteur (ou équivalent) et peut **se connecter** et **ouvrir** `/recruteur/*` **sans attendre** l’admin.
3. Tant que le compte n’est **pas approuvé** par l’admin (`recruteurApprouve = false` dans le JWT / profil), l’**interface** affiche un bandeau d’attente et **bloque** les actions (offres, pipeline, entretiens, etc.) — le backend doit refuser les mutations également.
4. L’**admin** traite la file dans `/admin/demandes-recruteur` :
   - **Approuver** → `recruteurApprouve = true` + **email** de confirmation.
   - **Rejeter** → compte reste connectable mais verrouillé (ou politique équipe) + **email de refus**.
5. Les **admins** ne s’inscrivent pas en ligne : seed ou création manuelle.

Il n’y a plus de parcours séparé « Devenir recruteur » depuis le profil candidat : un second compte recruteur passe par `/register/recruteur` si besoin.

## Emails (notification-service)

| Événement | Destinataire | Contenu minimal |
|-----------|--------------|-----------------|
| (optionnel) Nouveau recruteur inscrit | Admin | Alerte file de validation |
| Compte approuvé | Recruteur | Confirmation + lien espace |
| Compte refusé | Recruteur | Refus + support (optionnel) |

## Backend (à implémenter)

- **auth-service** : champs profil recruteur (entreprise, SIRET, etc.) + booléen **approuvé** ; endpoints **ADMIN** : liste recruteurs en attente, `POST .../approuver`, `POST .../rejeter` ; JWT avec claim `recruteurApprouve`.
- **notification-service** : envoi des emails après décision admin.
- **Gateway / services métier** : refuser les écritures offre/candidature pipeline côté recruteur si non approuvé (défense en profondeur, pas seulement le Thymeleaf).

## Frontend

- Formulaires `/register/candidat` et `/register/recruteur` ; fragment `fragments/recruteur-lock.html` + classe `hh-recruteur-locked` sur les pages recruteur.
- Démo : `?demo=recruteur_pending` pour simuler l’attente d’approbation.
