# PostgreSQL (HireHub)

Cible : **PostgreSQL 17** (image Docker `postgres:17-alpine`).

## Docker Compose

- **Hôte :** `localhost`
- **Port :** `5432`
- **Utilisateur / mot de passe :** `hirehub` / `hirehub` (développement uniquement)

## Bases par service

| Service | Base |
|---------|------|
| auth-service | `hirehub_auth` |
| offre-service | `hirehub_offre` |
| candidature-service | `hirehub_candidature` |
| entretien-service | `hirehub_entretien` |

## Spring Boot (exemple `application.yml`)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/hirehub_auth
    username: hirehub
    password: hirehub
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

Dépendances Maven à ajouter dans le module concerné :

- `org.springframework.boot:spring-boot-starter-data-jpa`
- `org.postgresql:postgresql`
