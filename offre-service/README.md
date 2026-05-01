# Offre Service

Microservice responsable du cycle de vie des offres d'emploi cote recruteur et de la lecture des offres cote candidat.

## Port et base de donnees

- Port local : `8082`
- Base PostgreSQL : `hirehub_offre`
- URL JDBC dev : `jdbc:postgresql://localhost:55432/hirehub_offre`
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
X-User-Id: UUID du recruteur connecte
X-User-Email: email du recruteur connecte
```

Exemple :

```text
X-User-Id: dbe2ed9a-3f1c-4052-97d6-bd6a46a68755
X-User-Email: recruteur.test@hirehub.local
```

## Guide apres pull

### 1. Recuperer le code

```bash
git pull origin main
```

### 2. Lancer l'infrastructure

Depuis la racine du projet :

```bash
docker compose up -d
```

Verifier que PostgreSQL, RabbitMQ et Mailpit sont demarres :

```bash
docker ps
```

### 3. Installer les modules Maven locaux

Depuis la racine du projet :

```bash
./mvnw -pl hirehub-common install
./mvnw -N install
```

Sur Windows PowerShell :

```powershell
.\mvnw.cmd -pl hirehub-common install
.\mvnw.cmd -N install
```

### 4. Lancer les services necessaires au test frontend

Lancer dans cet ordre :

```text
eureka-server       -> http://localhost:8761
api-gateway         -> http://localhost:8089
offre-service       -> http://localhost:8082
frontend-service    -> http://localhost:8086
```

Commandes utiles depuis la racine :

```powershell
.\mvnw.cmd -pl offre-service spring-boot:run
.\mvnw.cmd -pl frontend-service spring-boot:run
```

Si Maven ne trouve pas `hirehub-common`, relancer :

```powershell
.\mvnw.cmd -pl hirehub-common install
.\mvnw.cmd -N install
```

### 5. Tester dans le frontend

Se connecter avec un compte `RECRUTEUR`, puis tester :

```text
http://localhost:8086/recruteur/offres/nouvelle
```

Resultat attendu :

- creation d'une offre en statut `BROUILLON`
- l'offre apparait dans `http://localhost:8086/recruteur/offres`
- le bouton `Publier` passe le statut a `PUBLIEE`
- le bouton `Fermer` passe le statut a `FERMEE`
- seules les offres `PUBLIEE` apparaissent dans `http://localhost:8086/offres`
- les filtres publics `ville`, `typeContrat`, `motCle` fonctionnent

### 6. Tester via gateway / Postman

Base URL :

```text
http://localhost:8089/offre-service
```

Exemples :

```text
GET    /api/offres
GET    /api/offres?ville=Casablanca&typeContrat=CDI&motCle=java
GET    /api/offres/mes-offres
POST   /api/offres
PATCH  /api/offres/{id}/publier
PATCH  /api/offres/{id}/fermer
GET    /api/offres/{id}/valide
```

Pour les endpoints recruteur, ajouter les headers :

```text
X-User-Id: UUID du recruteur
X-User-Email: email du recruteur
```

## Tests

```bash
./mvnw -pl offre-service -am test
```

Sur Windows :

```powershell
.\mvnw.cmd -pl offre-service -am test
```
