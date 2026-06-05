# 3.5. Diagrammes de séquence

Les interactions ci-dessous décrivent les scénarios métier principaux de HireHub, tels qu’implémentés dans la branche de livraison : **frontend-service** (8086), **api-gateway** (8089), microservices métier via **Eureka**, messagerie **RabbitMQ** (exchange `hirehub.events`), **email-service** (8093, SMTP **Mailpit** en local) et **event-service** (audit).

---

## 3.5.1. Scénario : Postuler à une offre

1. Le candidat consulte une offre publiée puis accède à **`GET /offres/{id}/postuler`** (frontend-service).
2. Il soumet le formulaire (**CV PDF** + **lettre de motivation**, éventuellement enregistrés localement puis référencés par chemin).
3. Le frontend appelle **candidature-service** via la **gateway** avec le **JWT** du candidat : **`POST /candidatures`**.
4. **candidature-service** vérifie l’offre auprès de **offre-service** (Feign), contrôle l’unicité de la candidature, puis **enregistre la candidature en base PostgreSQL** avec le statut **SOUMISE**.
5. **candidature-service** publie sur RabbitMQ un événement **`candidature.created`** (format **EmailEventDTO**, routing key `candidature.created`).
6. **email-service** consomme la file `notif.candidature.queue` et envoie :
   - un **e-mail de confirmation au candidat** (`CANDIDATURE_CREATED`) ;
   - si l’offre contient l’e-mail du recruteur propriétaire, un **e-mail « nouvelle candidature » au recruteur** (`CANDIDATURE.RECRUITER_NEW`).
7. **event-service** consomme la file d’audit `audit.candidature.queue` et **enregistre l’événement** pour traçabilité.

*Figure 8 : diagramme de séquence — Postuler à une offre.*

---

## 3.5.2. Scénario : Changement de statut d’une candidature

1. Le recruteur consulte le **pipeline** de son offre (**`GET /recruteur/pipeline/{offreId}`**) ou ouvre le **détail** d’une candidature (**`GET /recruteur/candidature/{id}`**).
2. Il sélectionne un **nouveau statut autorisé** par la machine à états (ex. SOUMISE → EN_COURS ou REFUSEE ; ENTRETIEN → ACCEPTEE ou REFUSEE).
3. Le **frontend-service** transmet la demande à **candidature-service** via la gateway : **`PUT /candidatures/{id}/status?status={nouveauStatut}`** (et non PATCH).
4. **candidature-service** contrôle les droits (recruteur propriétaire de l’offre), **met à jour la candidature** en base et **insère une ligne dans `historique_statut`** (ancien statut, nouveau statut, commentaire, auteur).
5. **candidature-service** publie sur RabbitMQ un événement **`candidature.statut.changed`** (routing key `candidature.statut.changed`).
6. **email-service** consomme la file `notif.statut.queue` et **notifie le candidat par e-mail** (`CANDIDATURE_STATUT_CHANGED`).
7. **event-service** consomme la file d’audit `audit.statut.queue` et **trace l’action**.

*Figure 9 : diagramme de séquence — Changement de statut d’une candidature.*

---

## 3.5.3. Scénario : Validation du compte recruteur et notification

> **Note :** dans la version livrée, l’**approbation n’est plus déclenchée manuellement** par un bouton « Approuver » sur `/admin/demandes-recruteur`. Cette page sert au **suivi** des décisions. La validation est **automatisée** par **verification-service** après inscription.

1. Le recruteur termine son inscription (frontend → **auth-service** : création du compte, statut de vérification initial).
2. **auth-service** publie sur RabbitMQ l’événement **`recruiter.registered`** (routing key `recruiter.registered`).
3. **verification-service** consomme le message, exécute le **contrôle documentaire** (OCR / règles métier), puis publie le résultat **`recruiter.verified`**.
4. **auth-service** consomme **`recruiter.verified`**, met à jour le profil (**`recruiter_approved = true`** si décision APPROVED, sinon refus ou revue manuelle) et publie un événement e-mail selon le cas (`RECRUITER.APPROVED`, `RECRUITER.REJECTED` ou `RECRUITER.REVIEW_REQUIRED`).
5. **email-service** consomme la file `notif.recruiter.queue` et **envoie l’e-mail correspondant au recruteur** (confirmation d’approbation, refus ou demande de contrôle renforcé).
6. L’**administrateur** peut consulter **`GET /admin/demandes-recruteur`** pour **visualiser l’état** des inscriptions (approuvé, refusé, en cours, revue requise) — **lecture seule**.
7. **event-service** enregistre l’audit des événements recruteur sur les files `audit.recruiter.queue` lorsque applicable.

*Figure 10 : diagramme de séquence — Validation du compte recruteur.*

---

## 3.5.4. Scénario : Planification d’un entretien

1. Le recruteur ouvre le **détail d’une candidature** (**`/recruteur/candidature/{id}`**). La candidature doit être au statut **EN_COURS** (pas SOUMISE).
2. Il remplit le formulaire **« Planifier un entretien »** : date/heure future, type (**PRESENTIEL**, **VISIO** ou **TELEPHONIQUE**), **lieu** (obligatoire en présentiel) ou **lien visio** (obligatoire en visio).
3. Le **frontend-service** appelle **entretien-service** via la gateway : **`POST /entretiens`** (corps JSON + JWT ; `recruteurId` renseigné côté frontend).
4. **entretien-service** valide le créneau, **enregistre l’entretien** en base (statut **PLANIFIE**), puis appelle **candidature-service** en Feign : **`PUT /candidatures/{id}/status?status=ENTRETIEN`**.
5. **entretien-service** publie sur RabbitMQ un événement **`entretien.planifie`** (format **EmailEventDTO**, types `ENTRETIEN_PLANIFIE` ou `ENTRETIEN.ANNULATION` en cas d’annulation).
6. **email-service** consomme la file `notif.entretien.queue` et envoie un **e-mail au candidat** (convocation ou annulation). **Aucun e-mail automatique n’est envoyé au recruteur** à cette étape dans l’implémentation actuelle.
7. **event-service** consomme la file d’audit `audit.entretien.queue` et **enregistre l’audit**.

*Figure 11 : diagramme de séquence — Planification d’un entretien.*

---

## Synthèse des échanges HTTP principaux

| Scénario | Appel HTTP principal |
|----------|----------------------|
| Postuler | `POST /candidatures` |
| Statut | `PUT /candidatures/{id}/status?status=...` |
| Entretien | `POST /entretiens` puis `PUT` statut ENTRETIEN (Feign interne) |
| Validation recruteur | RabbitMQ `recruiter.registered` → `recruiter.verified` (pas de POST admin « approuver » en UI) |

## Synthèse des files RabbitMQ (notifications / audit)

| Routing key | Consumer notification | Consumer audit |
|-------------|----------------------|----------------|
| `candidature.created` | email-service | event-service |
| `candidature.statut.changed` | email-service | event-service |
| `entretien.planifie` | email-service | event-service |
| `recruiter.request.approved` / événements RECRUITER.* | email-service | event-service |
