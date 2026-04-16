# 🐰 RabbitMQ Configuration - HireHub

Guide complet pour configurer et utiliser RabbitMQ dans le projet HireHub.

---

## 🎯 Objectif du document

Ce document sert de **référence opérationnelle** pour tous les membres du projet.

➡️ Après lecture, tu dois être capable de :

* Comprendre l’architecture événementielle
* Publier des messages depuis un service
* Consommer des messages dans `notification-service`
* Debugger rapidement un problème RabbitMQ

---

## 📋 Table des matières

1. Architecture
2. Configuration automatique
3. Publier des messages
4. Consommer des messages
5. Tester avec RabbitMQ UI
6. Troubleshooting
7. Checklist

---

## 🏗️ Architecture

```
HIREHUB.EVENTS (Topic Exchange)
        |
        |-- candidature.created ---------> notif.candidature.queue
        |-- candidature.statut.changed --> notif.candidature.statut.queue
        |-- entretien.planifie ----------> notif.entretien.queue
        |-- recruiter.request.* ---------> notif.recruiter.queue

Toutes les queues → NotificationService (@RabbitListener)
```

### 🔑 Concepts clés

| Élément     | Description         |
| ----------- | ------------------- |
| Exchange    | Route les messages  |
| Routing Key | Type d’événement    |
| Queue       | Stocke les messages |
| Consumer    | Traite les messages |

---

## ⚙️ Configuration automatique

### 📦 Dépendance

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

---

### ⚡ Fonctionnement

Au démarrage de `notification-service` :

* Création automatique de l’exchange
* Création des queues
* Création des bindings

👉 **Aucune configuration manuelle dans RabbitMQ**

---

### ⚙️ application.yml

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: hirehub
    password: hirehub
    listener:
      simple:
        prefetch: 1
        default-requeue-rejected: true
```

---

## 📤 Publier des messages

### 🔹 Pattern standard

```java
rabbitTemplate.convertAndSend(
    EXCHANGE,
    ROUTING_KEY,
    event
);
```

---

### 📌 Cas d’usage

| Service             | Événement                  |
| ------------------- | -------------------------- |
| candidature-service | candidature.created        |
| entretien-service   | entretien.planifie         |
| auth-service        | recruiter.request.approved |

👉 Toujours envoyer un **DTO (Event)**

---

## 📥 Consommer des messages

### 🔹 Pattern standard

```java
@RabbitListener(queues = "nom.queue")
public void handle(@Payload String message) {
    // traiter message
}
```

---

### 📌 Bonnes pratiques

* Parser JSON → DTO
* Logguer chaque message
* Ne jamais bloquer le thread
* Gérer les erreurs proprement

---

## 🧪 Tester avec RabbitMQ UI

### 🔗 Accès

* URL: [http://localhost:15672](http://localhost:15672)
* user: hirehub
* password: hirehub

---

### ✔️ Vérifications

* Exchange présent
* Queues créées
* Bindings actifs

---

### 📤 Test rapide

1. Aller dans Exchange
2. Publier avec routing key
3. Vérifier queue

---

## 🔧 Troubleshooting

### ❌ Connexion refusée

```bash
docker-compose up -d rabbitmq
```

---

### ❌ Queue inexistante

```bash
docker-compose restart notification-service
```

---

### ❌ Messages non consommés

* Vérifier @RabbitListener
* Vérifier logs
* Vérifier consumers

---

## 🧠 Bonnes pratiques globales

* Utiliser des routing keys explicites
* Garder les events simples (DTO)
* Ne pas coupler les services
* Logger systématiquement

---

## ✅ Checklist

* [ ] RabbitMQ lancé
* [ ] notification-service OK
* [ ] Queues visibles
* [ ] Messages publiés
* [ ] Messages consommés

---

## 🚀 Prochaines étapes

* Implémenter les Events DTO
* Ajouter envoi d’emails
* Brancher tous les services

---

