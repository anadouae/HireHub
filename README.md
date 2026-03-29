# HireHub — PROJET JEE

Application type **ATS** : les entreprises publient des offres, les candidats postulent avec un CV (PDF), les recruteurs gèrent le pipeline (statuts, entretiens), l’admin supervise la plateforme (comptes, KPIs, logs).

**Dépôt GitHub :** [anadouae/HireHub---PROJET-JEE](https://github.com/anadouae/HireHub---PROJET-JEE)

## Architecture adoptée

- **Monorepo Maven multi-modules** : un module par microservice + `hirehub-common` pour les DTO partagés — idéal pour travailler en binôme sans conflits permanents.
- **Spring Boot 3.2** + **Spring Cloud 2023** : Eureka, Config Server, Gateway, OpenFeign.
- **PostgreSQL** : une base logique par service (initialisation via Docker).
- **Documentation** : `docs/ARCHITECTURE.md` (schéma, ports, checklist), `docs/COLLABORATION.md` (rôles, branches).

## Structure du dépôt

```
hirehub-parent (pom.xml)
├── hirehub-common/          # DTO / contrats partagés
├── eureka-server/           # :8761
├── config-server/           # :8888 (config native)
├── api-gateway/             # :8080
├── auth-service/            # :8081
├── offre-service/           # :8082
├── candidature-service/     # :8083 (+ Feign notification)
├── notification-service/    # :8084
├── entretien-service/       # :8085 (+ Feign notification)
├── frontend-service/        # :8086 Thymeleaf
├── docker/mysql/init/       # création des 4 bases
├── docker-compose.yml       # MySQL + Mailpit
└── docs/
```

## Prérequis

- JDK **17** (`JAVA_HOME` pointant vers le JDK sous Windows)
- Docker (PostgreSQL **17** + Mailpit en local)
- **Maven** : pas besoin d’installation globale — le dépôt inclut le **Maven Wrapper** (`mvnw` / `mvnw.cmd`).

## Commandes utiles

**Windows (PowerShell ou CMD), à la racine du projet :**

```text
.\mvnw.cmd -DskipTests package
```

**Linux / macOS :**

```bash
./mvnw -DskipTests package
chmod +x ./mvnw   # une seule fois si besoin
```

**Infra locale :**

```bash
docker compose up -d
```

**JDBC (exemple pour chaque service avec JPA) :**  
`jdbc:postgresql://localhost:5432/hirehub_auth` (adapter le nom de base : `hirehub_offre`, `hirehub_candidature`, `hirehub_entretien`). Utilisateur : `hirehub`, mot de passe : `hirehub` (développement uniquement — à externaliser en prod).

Interface Mailpit (emails de test) : http://localhost:8025  

## Publier sur GitHub

Depuis ce dossier (déjà relié à `origin`) :

```bash
git add .
git commit -m "chore: squelette architecture microservices HireHub"
git push origin main
```

Voir `CONTRIBUTING.md` pour le flux de collaboration.
