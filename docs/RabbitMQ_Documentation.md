# 📧 Notification Service - Documentation

## 📋 Vue d'ensemble

Le **Notification Service** est responsable de **recevoir les événements** provenant des autres services (candidature-service, entretien-service, auth-service) et d'**envoyer les notifications appropriées** aux utilisateurs.

C'est un service **basé sur RabbitMQ** qui utilise le pattern **Publisher-Subscriber** pour communiquer de manière asynchrone.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Autres Services                           │
│  (candidature-service, entretien-service, auth-service)     │
└──────────────────┬──────────────────────────────────────────┘
                   │ PUBLISH (RabbitTemplate)
                   ▼
┌─────────────────────────────────────────────────────────────┐
│                   RabbitMQ Exchange                          │
│            (hirehub.events - TopicExchange)                 │
└──────────────────┬──────────────────────────────────────────┘
                   │
        ┌──────────┼──────────┬─────────────┐
        │          │          │             │
    (routing keys avec patterns)
        │          │          │             │
        ▼          ▼          ▼             ▼
    ┌────┐    ┌────┐    ┌────┐        ┌────┐
    │ Q1 │    │ Q2 │    │ Q3 │        │ Q4 │
    └────┘    └────┘    └────┘        └────┘
        │          │          │             │
        └──────────┼──────────┴─────────────┘
                   │ CONSUME (@RabbitListener)
                   ▼
    ┌─────────────────────────────────────┐
    │   QueueConsumer (Notification Service) │
    │  - Traite les événements             │
    │  - Envoie les emails                 │
    └─────────────────────────────────────┘
```

---

## 📁 Structure des fichiers

```
notification-service/
├── src/main/java/com/hirehub/notification/
│   ├── NotificationServiceApplication.java      ← Point d'entrée
│   ├── config/
│   │   └── RabbitMQConfig.java                  ← Configuration RabbitMQ
│   ├── publisher/
│   │   └── EventPublisherFirst.java             ← Publier les événements
│   └── QueueConsumer.java                       ← Écouter et traiter
├── src/main/resources/
│   ├── application.yml                          ← Configuration du service
│   └── ...
└── pom.xml                                      ← Dépendances Maven
```

---

## 📄 Description détaillée des fichiers

### 1️⃣ **NotificationServiceApplication.java**

**Localisation:** `src/main/java/com/hirehub/notification/NotificationServiceApplication.java`

**Rôle:** Point d'entrée principal du service Notification.

**Code:**
```java
@EnableRabbit
@SpringBootApplication
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
```

**Responsabilités:**
- 🚀 Démarre l'application Spring Boot
- 🐰 Active le support RabbitMQ avec `@EnableRabbit`
- 📦 Configure l'auto-scan des composants Spring

**Annotations importantes:**
- `@EnableRabbit`: Active les listeners RabbitMQ (permet à `@RabbitListener` de fonctionner)
- `@SpringBootApplication`: Active auto-configuration Spring Boot

**Dépendances créées automatiquement:**
- `RabbitTemplate` - pour publier les messages
- `@RabbitListener` - reconnu dans les autres classes

---

### 2️⃣ **RabbitMQConfig.java**

**Localisation:** `src/main/java/com/hirehub/notification/config/RabbitMQConfig.java`

**Rôle:** Configurer toute l'infrastructure RabbitMQ (Exchange, Queues, Bindings).

**Architecture RabbitMQ:**
```
Publisher → Message → Exchange → Routing Key → Binding → Queue → Consumer
```

**Éléments configurés:**

#### A) **TopicExchange** (L'aiguilleur)
```java
@Bean
public TopicExchange hirehubExchange() {
    return new TopicExchange(
        RabbitMQConstants.EXCHANGE,  // Nom: "hirehub.events"
        true,    // durable = persiste après redémarrage
        false    // autoDelete = ne pas auto-supprimer
    );
}
```

**Qu'est-ce qu'un Exchange?**
- C'est un **aiguilleur central** qui reçoit tous les messages
- Basé sur les **routing keys**, il dirige les messages vers les bonnes queues
- Type `Topic` permet les patterns (ex: `candidature.*` match `candidature.created` ET `candidature.statut`)

---

#### B) **Queues** (Les boîtes aux lettres)

Le service crée **4 queues** pour les 4 types d'événements:

| Nom | Constant | Événements | Consommateur | Exemple |
|-----|----------|-----------|--------------|---------|
| `notif.candidature.queue` | `QUEUE_NOTIFICATION_CANDIDATURE_CREATED` | `candidature.created` | QueueConsumer | Nouvelle candidature reçue |
| `notif.statut.queue` | `QUEUE_NOTIFICATION_CANDIDATURE_STATUT` | `candidature.statut.changed` | QueueConsumer | Statut de candidature changé |
| `notif.entretien.queue` | `QUEUE_NOTIFICATION_ENTRETIEN` | `entretien.planifie` | QueueConsumer | Entretien planifié/annulé |
| `notif.recruiter.queue` | `QUEUE_NOTIFICATION_RECRUITER` | `recruiter.request.*` | QueueConsumer | Recruteur approuvé/rejeté |

**Caractéristiques:**
- ✅ `durable=true`: Survit aux redémarrages RabbitMQ
- ✅ `exclusive=false`: Accessible à plusieurs consommateurs
- ✅ `autoDelete=false`: Ne pas supprimer automatiquement

**Code exemple:**
```java
@Bean
public Queue notificationCandidatureCreatedQueue() {
    return new Queue(
        RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE_CREATED,
        true,   // durable
        false,  // exclusive
        false   // autoDelete
    );
}
```

---

#### C) **Bindings** (Les connexions)

Chaque Binding connecte un Queue à l'Exchange avec une **routing key**:

```java
@Bean
public Binding bindingCandidatureCreated(
    @Qualifier("notificationCandidatureCreatedQueue") Queue queue,
    TopicExchange exchange
) {
    return BindingBuilder
        .bind(queue)
        .to(exchange)
        .with(RabbitMQConstants.ROUTING_CANDIDATURE_CREATED);
        // "with" = routing key de matching
}
```

**Comment ça marche?**
1. Une service publie un message avec routing key = `"candidature.created"`
2. L'Exchange reçoit le message
3. L'Exchange cherche les Bindings qui matchent cette routing key
4. Le Binding dit: "Si la routing key est `candidature.created`, mets le message dans `notif.candidature.queue`"
5. Le Consumer écoute la queue et traite le message

**Les 4 Bindings:**

| Binding | Exchange → Queue | Routing Key |
|---------|------------------|-------------|
| bindingCandidatureCreated | hirehub.events → notif.candidature.queue | `candidature.created` |
| bindingStatutChanged | hirehub.events → notif.statut.queue | `candidature.statut.changed` |
| bindingEntretienPlanifie | hirehub.events → notif.entretien.queue | `entretien.planifie` |
| bindingRecruiterDecision | hirehub.events → notif.recruiter.queue | `recruiter.request.*` |

---

### 3️⃣ **QueueConsumer.java**

**Localisation:** `src/main/java/com/hirehub/notification/QueueConsumer.java`

**Rôle:** Écouter les 4 queues et **traiter les événements** reçus.

**Pattern:** `@RabbitListener` = "S'abonner à une queue et appeler cette méthode quand un message arrive"

#### Listener 1: `handleCandidatureCreated()`

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE_CREATED)
public void handleCandidatureCreated(@Payload String message) { ... }
```

**Événement:** `candidature.created`  
**Source:** candidature-service (quand un candidat soumet une candidature)  
**Action:** Envoyer email "Candidature reçue" au candidat  
**Message JSON attendu:**
```json
{
  "candidatureId": 42,
  "candidatEmail": "jean@example.com",
  "offreTitle": "Développeur Java Senior",
  "candidatName": "Jean Dupont"
}
```

**TODO:**
- [ ] Parser le JSON
- [ ] Récupérer les détails du candidat et de l'offre
- [ ] Générer le template email
- [ ] Envoyer via MailPit

---

#### Listener 2: `handleStatutChanged()`

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_CANDIDATURE_STATUT)
public void handleStatutChanged(@Payload String message) { ... }
```

**Événement:** `candidature.statut.changed`  
**Source:** candidature-service (quand le recruteur change le statut)  
**Action:** Envoyer email "Statut mis à jour" au candidat  
**Message JSON attendu:**
```json
{
  "candidatureId": 42,
  "candidatEmail": "jean@example.com",
  "ancienStatut": "EN_ATTENTE",
  "nouveauStatut": "ACCEPTÉ",
  "commentaire": "Bravo, vous êtes sélectionné!"
}
```

**Statuts possibles:**
- ❌ `REJETÉ` → Email de rejet
- ⏳ `EN_ATTENTE` → Email d'attente
- ✅ `ACCEPTÉ` → Email de bienvenue pour l'entretien
- 👤 `À_ÉVALUER` → Email de notification

---

#### Listener 3: `handleEntretienPlanifie()`

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_ENTRETIEN)
public void handleEntretienPlanifie(@Payload String message) { ... }
```

**Événement:** `entretien.planifie`  
**Source:** entretien-service (quand un entretien est planifié/annulé)  
**Action:** Envoyer email avec date/heure/lieu de l'entretien  
**Message JSON attendu:**
```json
{
  "entretienId": 15,
  "candidatEmail": "jean@example.com",
  "candidatName": "Jean Dupont",
  "dateEntretien": "2026-04-15T10:30:00",
  "lieu": "Bureau Paris, 3e étage, Salle A",
  "interviewerName": "Marie Durand",
  "interviewerEmail": "marie@hirehub.com"
}
```

**TODO:**
- [ ] Parser le JSON
- [ ] Générer le template email avec date/heure/lieu
- [ ] Optionnel: Créer une invitation calendar (.ics)
- [ ] Envoyer aux candidat ET interviewer

---

#### Listener 4: `handleRecruiterDecision()`

```java
@RabbitListener(queues = RabbitMQConstants.QUEUE_NOTIFICATION_RECRUITER)
public void handleRecruiterDecision(@Payload String message) { ... }
```

**Événement:** `recruiter.request.*` (approved ou rejected)  
**Source:** auth-service (quand l'admin approuve/rejette une demande recruteur)  
**Action:** Envoyer email d'approbation ou de rejet  
**Message JSON attendu:**
```json
{
  "requestId": 7,
  "email": "newrecruiter@company.com",
  "name": "Alice Dupont",
  "status": "APPROVED",
  "message": "Bienvenue! Vous pouvez maintenant créer des offres"
}
```

**Deux branches:**
- **APPROVED** → "Bienvenue, vous êtes maintenant recruteur!"
- **REJECTED** → "Désolé, votre demande a été rejetée"

---

### 4️⃣ **EventPublisherFirst.java**

**Localisation:** `src/main/java/com/hirehub/notification/publisher/EventPublisherFirst.java`

**Rôle:** Fournir des **méthodes prêtes à l'emploi** pour publier des événements depuis n'importe quel service.

**Note:** Ce fichier est surtout utilisé comme **référence/exemple**. Les autres services utiliseraient `RabbitTemplate` directement.

#### Méthodes de publication:

##### 1. `publishCandidatureCreated(Object event)`
```java
public void publishCandidatureCreated(Object event) {
    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE,
        RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
        event
    );
}
```
**Quand l'utiliser:** Depuis candidature-service quand une candidature est créée

---

##### 2. `publishStatutChanged(Object event)`
```java
public void publishStatutChanged(Object event) {
    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE,
        RabbitMQConstants.ROUTING_CANDIDATURE_STATUT_CHANGED,
        event
    );
}
```
**Quand l'utiliser:** Depuis candidature-service quand le statut change

---

##### 3. `publishEntretienPlanifie(Object event)`
```java
public void publishEntretienPlanifie(Object event) {
    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE,
        RabbitMQConstants.ROUTING_ENTRETIEN_PLANIFIE,
        event
    );
}
```
**Quand l'utiliser:** Depuis entretien-service quand un entretien est planifié

---

##### 4. `publishRecruiterApproved(Object event)` & `publishRecruiterRejected(Object event)`
```java
public void publishRecruiterApproved(Object event) { ... }
public void publishRecruiterRejected(Object event) { ... }
```
**Quand l'utiliser:** Depuis auth-service quand une demande recruteur est approuvée/rejetée

---

##### 5. `publishEvent(String routingKey, Object event)` (Générique)
```java
public void publishEvent(String routingKey, Object event) {
    rabbitTemplate.convertAndSend(
        RabbitMQConstants.EXCHANGE,
        routingKey,
        event
    );
}
```
**Quand l'utiliser:** Pour des événements custom/futurs

---

## 🔄 Flux complet d'un événement

### Exemple: Nouvelle candidature reçue

```
┌─────────────────────────────────────────────────────────┐
│ 1. CANDIDATURE-SERVICE                                   │
│    • Candidat soumet une candidature                    │
│    • Base de données sauvegardée                        │
│                                                          │
│    Code:                                                 │
│    candidatureRepository.save(candidature);             │
│    eventPublisher.publishCandidatureCreated(candidature);
└────────────────────┬────────────────────────────────────┘
                     │
                     │ RabbitTemplate.convertAndSend(
                     │   EXCHANGE="hirehub.events",
                     │   ROUTING_KEY="candidature.created",
                     │   event={...}
                     │ )
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 2. RABBITMQ EXCHANGE (hirehub.events)                   │
│    • Reçoit le message avec routing key                 │
│    • Cherche les bindings qui matchent                 │
│    • Trouve: "candidature.created" → notif.candidature.queue
└────────────────────┬────────────────────────────────────┘
                     │
                     │ Route le message
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 3. QUEUE (notif.candidature.queue)                      │
│    • Message en attente dans la queue                   │
│    • Prêt à être consommé                              │
└────────────────────┬────────────────────────────────────┘
                     │
                     │ QueueConsumer écoute la queue
                     ▼
┌─────────────────────────────────────────────────────────┐
│ 4. NOTIFICATION-SERVICE (QueueConsumer)                 │
│    @RabbitListener appelle handleCandidatureCreated()  │
│                                                          │
│    Code:                                                 │
│    - Parse le JSON du message                           │
│    - Récupère email du candidat                         │
│    - Génère le template email                           │
│    - Envoie via MailPit/Service d'email                │
│    - Log: "[✅] Email envoyé"                           │
└─────────────────────────────────────────────────────────┘
```

---

## 🚀 Comment utiliser ce service depuis un autre service

### Exemple: Depuis candidature-service

**1. Injecter RabbitTemplate:**
```java
@Service
public class CandidatureService {
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    public void createCandidature(Candidature candidature) {
        // Sauvegarder en base
        candidatureRepository.save(candidature);
        
        // Publier l'événement
        rabbitTemplate.convertAndSend(
            RabbitMQConstants.EXCHANGE,
            RabbitMQConstants.ROUTING_CANDIDATURE_CREATED,
            candidature
        );
    }
}
```

**2. Ou utiliser EventPublisherFirst (si injecté):**
```java
@Service
public class CandidatureService {
    
    @Autowired
    private EventPublisherFirst eventPublisher;
    
    public void createCandidature(Candidature candidature) {
        candidatureRepository.save(candidature);
        eventPublisher.publishCandidatureCreated(candidature);
    }
}
```

---

## ⚙️ Configuration (application.yml)

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
```

---

## 🧪 Tester les événements

### Option 1: Avec la CLI RabbitMQ (dans le conteneur)

```bash
# Accéder au conteneur RabbitMQ
docker exec -it hirehub-rabbitmq /bin/bash

# Publier un message
rabbitmqctl publish_message hirehub.events candidature.created '{"candidatureId": 42}'
```

### Option 2: Interface Management RabbitMQ

- Accès: http://localhost:15672
- Username: `hirehub`
- Password: `hirehub`

---

## 📊 Diagramme UML des Classes

```
┌─────────────────────────────────────┐
│   NotificationServiceApplication    │
│  ──────────────────────────────────  │
│  - @EnableRabbit                    │
│  - @SpringBootApplication           │
│  + main(String[])                  │
└─────────────────────────────────────┘
           │
           ├─────────────────────────┬─────────────────────────┐
           │                         │                         │
           ▼                         ▼                         ▼
┌──────────────────────┐  ┌──────────────────────┐  ┌────────────────────┐
│ RabbitMQConfig       │  │ QueueConsumer        │  │ EventPublisherFirst│
│ ──────────────────── │  │ ──────────────────── │  │ ──────────────────  │
│ @Configuration       │  │ @Component           │  │ @Service           │
│                      │  │                      │  │                    │
│ + hirehubExchange()  │  │ + handleCandidature()│  │ + publishCandidature()
│ + queue1/2/3/4()     │  │ + handleStatut()     │  │ + publishStatut()   │
│ + binding1/2/3/4()   │  │ + handleEntretien()  │  │ + publishEntretien()
│                      │  │ + handleRecruiter()  │  │ + publishRecruiter()
└──────────────────────┘  └──────────────────────┘  └────────────────────┘
```

---

## ❓ FAQ

**Q: Pourquoi RabbitMQ et pas une base de données?**  
R: RabbitMQ est un **message broker** - c'est fait pour les communications **asynchrones** entre services. C'est plus rapide et découple les services.

**Q: Qu'est-ce qui arrive si le service est down?**  
R: Les messages restent dans les queues RabbitMQ et seront traités dès que le service redémarre.

**Q: Pourquoi 4 queues et pas 1?**  
R: Pour la **scalabilité** - on peut avoir plusieurs instances du service, chacune écoutant une queue.

**Q: Comment ajouter un nouvel événement?**  
R:
1. Ajouter une constante dans `RabbitMQConstants` (hirehub-common)
2. Créer une nouvelle Queue dans `RabbitMQConfig`
3. Créer un nouveau Binding dans `RabbitMQConfig`
4. Ajouter un nouveau `@RabbitListener` dans `QueueConsumer`

---

## 📚 Ressources

- [RabbitMQ Concepts](https://www.rabbitmq.com/tutorials/amqp-concepts.html)
- [Spring AMQP Documentation](https://spring.io/projects/spring-amqp)
- [Topic Exchange Pattern](https://www.rabbitmq.com/tutorials/tutorial-five-java.html)

---

**Dernière mise à jour:** 16 Avril 2026  
**Auteur:** Deep-Coding15 - HireHub Team

