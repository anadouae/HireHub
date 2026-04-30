# Offre Service

Microservice responsable du cycle de vie des offres d'emploi cote recruteur et de la lecture des offres cote candidat.

## Port et base de donnees

- Port local : `8082`
- Base PostgreSQL : `hirehub_offre`
- URL JDBC dev : `jdbc:postgresql://localhost:5433/hirehub_offre`
- Utilisateur / mot de passe dev : `hirehub` / `hirehub`

## Regles metier

- Une offre creee commence en statut `BROUILLON`.
- Seul le recruteur proprietaire (`X-User-Id`) peut modifier, publier ou fermer son offre.
- La liste publique retourne les offres `PUBLIEE`.
- Une candidature est autorisee uniquement si `GET /api/offres/{id}/valide` retourne `true`.

## Contrat API

| Methode | URL | Description |
| --- | --- | --- |
| POST | `/api/offres` | Creer une offre brouillon |
| GET | `/api/offres` | Liste paginee avec filtres `ville`, `typeContrat`, `motCle` |
| GET | `/api/offres/{id}` | Detail d'une offre |
| GET | `/api/offres/mes-offres` | Liste des offres du recruteur connecte |
| PUT | `/api/offres/{id}` | Modifier une offre |
| PATCH | `/api/offres/{id}/publier` | Publier une offre |
| PATCH | `/api/offres/{id}/fermer` | Fermer une offre |
| GET | `/api/offres/{id}/valide` | Verifier si une offre est publiee |

## Exemple JSON

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

Headers recruteur attendus apres passage par le gateway :

```text
X-User-Id: 10
X-User-Email: rh@example.com
```

## Tests

```bash
./mvnw -pl offre-service -am test
```
