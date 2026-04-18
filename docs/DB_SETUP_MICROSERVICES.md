# Setup bases de donnees - HireHub

## Bases PostgreSQL a creer

- `hirehub_auth`
- `hirehub_offre`
- `hirehub_candidature`
- `hirehub_entretien`

Le script `docker/postgres/init/01-databases.sql` cree ces bases automatiquement au premier demarrage du conteneur `postgres`.

## Demarrage de l'infrastructure

Depuis la racine du projet:

```powershell
docker compose up -d
```

Verifier:

```powershell
docker ps
```

## Config des microservices

- `auth-service` -> `jdbc:postgresql://localhost:55432/hirehub_auth`
- `offre-service` -> `jdbc:postgresql://localhost:55432/hirehub_offre`
- `candidature-service` -> `jdbc:postgresql://localhost:55432/hirehub_candidature`
- `entretien-service` -> `jdbc:postgresql://localhost:55432/hirehub_entretien`

Tous utilisent:

- utilisateur: `hirehub`
- mot de passe: `hirehub`
- `ddl-auto: update` (dev uniquement)

## Important

Si tu as deja demarre postgres avant modification du script init:

1. Arreter les conteneurs:
   ```powershell
   docker compose down
   ```
2. Supprimer le volume Postgres (reinitialise les donnees):
   ```powershell
   docker volume rm hirehub-projet-jee_hirehub-pg-data
   ```
3. Redemarrer:
   ```powershell
   docker compose up -d
   ```

Alternative sans supprimer volume: creer les DB manuellement avec psql/pgAdmin.
