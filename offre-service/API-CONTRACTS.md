# API Contracts - Offre Service

Base path service : `/api/offres`

## Headers recruteur

Les endpoints recruteur attendent les headers transmis par le gateway apres validation JWT :

```text
X-User-Id: 10
X-User-Email: rh@example.com
```

## Endpoints

| Methode | URL | Description |
| --- | --- | --- |
| POST | `/api/offres` | Creer une offre en brouillon |
| GET | `/api/offres` | Lister les offres publiees avec pagination et filtres |
| GET | `/api/offres/{id}` | Lire le detail d'une offre |
| GET | `/api/offres/mes-offres` | Lister les offres du recruteur connecte |
| PUT | `/api/offres/{id}` | Modifier une offre du recruteur connecte |
| PATCH | `/api/offres/{id}/publier` | Publier une offre du recruteur connecte |
| PATCH | `/api/offres/{id}/fermer` | Fermer une offre du recruteur connecte |
| GET | `/api/offres/{id}/valide` | Retourner `true` si l'offre est publiee |

## Query params liste publique

```text
GET /api/offres?ville=Casa&typeContrat=CDI&motCle=java&page=0&size=10
```

- `ville` : optionnel
- `typeContrat` : optionnel, valeurs `CDI`, `CDD`, `STAGE`, `ALTERNANCE`, `FREELANCE`
- `motCle` : optionnel, cherche dans le titre et la description
- `page`, `size`, `sort` : pagination Spring

## Request creation / modification

```json
{
  "titre": "Developpeur Java",
  "description": "Developpement backend Spring Boot",
  "typeContrat": "CDI",
  "ville": "Casablanca",
  "salaire": 12000,
  "dateExpiration": "2026-12-31T23:59:00"
}
```

## Reponse offre

```json
{
  "id": 1,
  "titre": "Developpeur Java",
  "description": "Developpement backend Spring Boot",
  "typeContrat": "CDI",
  "ville": "Casablanca",
  "salaire": 12000,
  "dateCreation": "2026-04-29T18:00:00",
  "dateExpiration": "2026-12-31T23:59:00",
  "statut": "BROUILLON",
  "recruteurId": 10,
  "recruteurEmail": "rh@example.com"
}
```

## Erreurs attendues

| Cas | Statut |
| --- | --- |
| Offre inconnue | `404 Not Found` |
| Recruteur non proprietaire | `403 Forbidden` |
| Validation DTO invalide | `400 Bad Request` |
