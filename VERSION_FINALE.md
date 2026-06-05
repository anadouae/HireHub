# HireHub — version finale (état du projet)

Document de référence pour la soutenance et le démarrage local.

## Ce qui a été finalisé

### Frontend (Douae)
- UI professionnelle : plus de textes techniques (`offre-service`, `candidature-service`, etc.).
- Pied de page : lien « Demander une démo » retiré.
- **Postulation** : `POST /offres/{id}/postuler` avec upload CV + lettre.
- **Mes candidatures** : données réelles via gateway + JWT en session.
- JWT généré à la connexion pour appeler `candidature-service` en sécurité.

### Microservices
- **Offre** : port **8092** (évite conflit Windows sur 8082).
- **Email / notification** : module `email-service`, port **8093**, consumers RabbitMQ.
- **Event** : audit métier (port 8084).
- Build Maven global : `.\mvnw.cmd -DskipTests package` OK.

---

## Schéma des flux

```
[Frontend :8086]
      │ HTTP + JWT (session)
      ▼
[API Gateway :8089]
      │
      ├── offre-service :8092
      ├── candidature-service :8083 ──► RabbitMQ ──► email-service :8093 (mails)
      │                                      └──► event-service :8084 (audit)
      ├── auth-service :8081
      └── entretien-service :8085
```

---

## Nommage notification vs email

| Nom équipe / doc | Nom dans le repo |
|------------------|------------------|
| notification-service (métier) | `email-service` (module Maven) |
| `EmailServiceApplication` | Classe à lancer |
| `NotificationPublisher` | Dans `hirehub-common` (publication RabbitMQ) |

---

## Ports (mémo)

```
8761 Eureka | 8888 Config | 8081 Auth | 8092 Offre | 8083 Candidature
8084 Event | 8085 Entretien | 8093 Email | 8090 Verification
8089 Gateway | 8086 Frontend
55432 PostgreSQL | 5672 RabbitMQ | 8025 Mailpit
```

---

## Fichiers importants

| Fichier | Contenu |
|---------|---------|
| `DEMARRAGE_RAPIDE.md` | Guide de lancement pas à pas |
| `docs/SCHEMA_BASES_DONNEES.md` | Tables PostgreSQL |
| `docs/postman/HireHub-local.postman_collection.json` | **Collection Postman unique** (ports à jour, gateway + tous services) |
| `postman/HireHub.postman_collection.json` | Ancienne collection offres/gateway (remplacée par le fichier ci-dessus) |
| `hirehub-common/.../RabbitMQConstants.java` | Queues / routing keys |

---

## Équipe — rappel des rôles

| Personne | Modules |
|----------|---------|
| **Douae El Assal** | frontend, auth, verification, admin |
| **Imane** | offre-service |
| **Lydivine** | candidature, emails (avec email-service), pipeline |
| **Wissal** | entretien-service |

---

## Avant la soutenance

1. `docker compose up -d` + `.\scripts\setup-databases.ps1`
2. Lancer les 11 services (voir `DEMARRAGE_RAPIDE.md`)
3. Tester candidat + recruteur + un e-mail dans Mailpit
4. Captures d'écran des parcours principaux
