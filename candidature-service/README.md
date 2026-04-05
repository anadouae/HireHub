# 🎯 Candidature Service

Service de gestion des candidatures des candidats aux offres d'emploi.

## 📋 Table des matières

- [Architecture](#architecture)
- [Configuration](#configuration)
- [API REST](#api-rest)
- [Démarrage](#démarrage)
- [Tests](#tests)

## 🏗️ Architecture

### Stack Technique
- Spring Boot 3.2.5
- PostgreSQL (production) / En mémoire (mock)
- JPA/Hibernate
- RabbitMQ
- Eureka Client

### Port
**8083**

### Profils
- `mock` - Données en mémoire pour développement
- `production` - PostgreSQL + RabbitMQ

## ⚙️ Configuration

### Mode Mock (Développement)
```yaml
spring:
  profiles:
    active: mock
```

**Utilise**: `CandidatureServiceMock` avec 5 candidatures pré-chargées

### Mode Production
```yaml
spring:
  profiles:
    active: production
  datasource:
    url: jdbc:postgresql://localhost:5432/hirehub_candidature
    username: hirehub_user
    password: hirehub_password
  rabbitmq:
    host: localhost
    port: 5672
```

**Utilise**: `CandidatureServiceImpl` avec PostgreSQL et RabbitMQ

## 📡 API REST

### Endpoints

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| POST | `/candidatures` | Créer une candidature |
| GET | `/candidatures/moi` | Mes candidatures (Candidat) |
| GET | `/candidatures/offre/{offreId}` | Candidatures d'une offre (Recruteur) |
| GET | `/candidatures/{id}` | Détails d'une candidature |
| PUT | `/candidatures/{id}/status` | Mettre à jour le statut |
| PATCH | `/candidatures/{id}` | Mettre à jour les fichiers |
| GET | `/candidatures/{id}/historique` | Historique des changements |
| POST | `/candidatures/{id}/cv-cl` | Upload CV et lettre |
| DELETE | `/candidatures/{id}` | Supprimer une candidature |

### Exemple de Requête

```bash
# Créer une candidature
curl -X POST http://localhost:8083/candidatures \
  -H "Content-Type: application/json" \
  -d '{
    "candidatId": "user-123",
    "offreId": "offre-456",
    "CV_Path": "/uploads/cv.pdf",
    "lettreMotivationPath": "/uploads/cover.pdf"
  }'

# Récupérer mes candidatures
curl http://localhost:8083/candidatures/moi

# Mettre à jour le statut (Recruteur)
curl -X PUT http://localhost:8083/candidatures/cand-001/status?newStatus=ACCEPTED
```

## 🚀 Démarrage

### Prérequis
- Java 17+
- PostgreSQL 14+ (mode production)
- RabbitMQ 3.12+ (mode production)
- Eureka Server sur `localhost:8761`

### Compiler

```bash
cd candidature-service
mvnw clean compile
```

### Lancer (Mode Mock)

```bash
mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=mock"
```

### Lancer (Mode Production)

```bash
# Assurer que PostgreSQL et RabbitMQ tournent
mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=production"
```

## 🧪 Tests

### Mode Mock - Tests avec Données Pré-chargées

5 candidatures sont automatiquement chargées au démarrage :

```json
[
  { "id": "cand-001", "candidatId": "user-john-001", "status": "EN_ATTENTE" },
  { "id": "cand-002", "candidatId": "user-alice-002", "status": "ACCEPTED" },
  { "id": "cand-003", "candidatId": "user-bob-003", "status": "REJECTED" },
  { "id": "cand-004", "candidatId": "user-carol-004", "status": "EN_ATTENTE" },
  { "id": "cand-005", "candidatId": "user-diana-005", "status": "INTERVIEW" }
]
```

### Tester avec Postman/cURL

Voir le fichier `CURL_EXAMPLES.md` pour des exemples complets.

## 📦 Entités

### Candidature
```java
@Entity
public class Candidature {
    String id;                        // UUID
    String candidatId;                // Candidat
    String offreId;                   // Offre
    String CV_Path;                   // Chemin du CV
    String lettreMotivationPath;      // Chemin de la lettre
    CandidatureStatus status;         // EN_ATTENTE, ACCEPTED, REJECTED, INTERVIEW
    LocalDateTime dateSoumission;     // Date de création
    LocalDateTime dateModification;   // Dernière modification
}
```

### HistoriqueStatus
```java
@Entity
public class HistoriqueStatus {
    String id;                        // UUID
    String candidatureId;             // Référence candidature
    CandidatureStatus ancienStatus;   // Ancien statut
    CandidatureStatus nouveauStatus;  // Nouveau statut
    String commentaire;               // Remarques du recruteur
    String utilisateurId;             // ID du recruteur
    LocalDateTime dateChangement;     // Date du changement
}
```

## 📡 Événements RabbitMQ

### Events Publiés

#### 1. `candidature.created`
Publié quand une nouvelle candidature est créée.

```json
{
  "candidatureId": "cand-uuid",
  "candidatId": "user-123",
  "offreId": "offre-456",
  "dateSoumission": "2026-04-02T15:30:00"
}
```

**Consumers**: `notification-service` (envoie email de confirmation)

#### 2. `candidature.statut.changed`
Publié quand le statut d'une candidature change.

```json
{
  "candidatureId": "cand-uuid",
  "candidatId": "user-123",
  "offreId": "offre-456",
  "ancienStatut": "EN_ATTENTE",
  "nouveauStatut": "ACCEPTED",
  "dateChangement": "2026-04-02T16:00:00"
}
```

**Consumers**: `notification-service` (envoie email de notification)

## 🔐 Authentification (TODO)

Actuellement sans authentification. À implémenter :
- [ ] Spring Security + OAuth2
- [ ] Récupérer `candidatId` depuis JWT
- [ ] Vérifier les autorisations (candidat ne modifie que ses candidatures)

## 📊 Statuts Possibles

| Statut | Description |
|--------|-------------|
| `EN_ATTENTE` | En attente d'examen |
| `INTERVIEW` | Appelée pour entretien |
| `ACCEPTED` | Acceptée |
| `REJECTED` | Rejetée |

## 🐛 Logs

```
[INFO] Création d'une candidature pour l'offre: offre-456
[INFO] Candidature créée avec l'ID: cand-uuid
[INFO] Événement 'candidature.created' publié pour la candidature: cand-uuid
```

## 📁 Structure des Fichiers

```
candidature-service/
├── src/main/java/com/hirehub/candidature/
│   ├── Candidature.java
│   ├── HistoriqueStatus.java
│   ├── CandidatureController.java
│   ├── CandidatureServiceApplication.java
│   ├── services/
│   │   ├── CandidatureService.java
│   │   ├── CandidatureServiceImpl.java
│   │   └── CandidatureServiceMock.java
│   ├── repository/
│   │   ├── CandidatureRepository.java
│   │   └── HistoriqueStatusRepository.java
│   ├── config/
│   │   └── RabbitMQPublisherConfig.java
│   └── dtos/
│       └── CandidatureDTO.java
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/
│       └── V1__Create_candidature_tables.sql
└── pom.xml
```

## 🔗 Dépendances

- auth-service (vérifier candidat/recruteur)
- offre-service (vérifier offre)
- notification-service (recevoir notifications via RabbitMQ)
- eureka-server (service discovery)

## 📞 Support

Voir la documentation principale du projet.

