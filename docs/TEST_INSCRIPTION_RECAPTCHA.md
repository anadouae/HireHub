# Guide test inscription + login + admin

## Pourquoi le `403` sur `http://localhost:8081/api/v1/auth/register/recruteur` ?

Ce endpoint est une API backend de `auth-service` qui attend un **POST multipart**.
Si vous ouvrez cette URL directement dans le navigateur, vous faites un **GET**.
Donc ce n'est pas le bon usage de l'endpoint.

Utilisez les formulaires du frontend:

- `http://localhost:8086/register/candidat`
- `http://localhost:8086/register/recruteur`

## Je ne vois pas de case reCAPTCHA a cocher

Avec les cles **Score / Enterprise (v3)**, reCAPTCHA est **invisible** : pas de puzzle ni de case « Je ne suis pas un robot ».
Vous remplissez le formulaire, vous cochez **J’accepte la verification documentaire** (obligatoire pour le recruteur), puis vous cliquez sur **Créer mon compte**. Le script envoie alors un jeton a Google en arriere-plan. Le **badge** reCAPTCHA en bas a droite confirme que le script est charge.

Pour un **vrai** test avec case a cocher, il faudrait une cle **reCAPTCHA v2 case a cocher** (console classique) et revenir au widget `g-recaptcha` dans le HTML (non compatible avec une cle « Score » seule).

## Configuration reCAPTCHA Google

## 1) Frontend (`frontend-service/src/main/resources/application.yml`)

```yaml
hirehub:
  recaptcha:
    site-key: "VOTRE_SITE_KEY"
```

## 2) Auth (`auth-service/src/main/resources/application.yml`)

```yaml
hirehub:
  recaptcha:
    enabled: true
    secret-key: "VOTRE_SECRET_KEY"
    verify-url: https://www.google.com/recaptcha/api/siteverify
```

Notes:

- en local sans clé Google, vous pouvez laisser `enabled: false`
- si `enabled: true` et clé manquante, l'inscription est refusée

### Erreur : « Localhost is not in the list of supported domains »

Google n’autorise le widget reCAPTCHA que sur les **domaines déclarés** pour cette paire de clés.

1. Ouvrez [Google reCAPTCHA Admin](https://www.google.com/recaptcha/admin).
2. Sélectionnez le site (la clé que vous utilisez).
3. Dans les paramètres du site, ajoutez aux **domaines** au minimum :
   - `localhost`
   - `127.0.0.1` (utile si vous ouvrez l’app via cette adresse)
4. Enregistrez, attendez **1 à 2 minutes**, puis rechargez la page d’inscription (`Ctrl+F5`).

Si vous testez derrière un autre hôte (ex. `192.168.x.x`), ajoutez aussi ce domaine ou l’IP.

**Type de clé :** le frontend utilise **reCAPTCHA v3 / Score** (`grecaptcha.execute` avec l’action `register`). Une clé **v2 case à cocher** provoque l’erreur **« Invalid key type »** : il faut une clé **Score** (ou v3) comme dans Google Cloud reCAPTCHA Enterprise.

**Backend :** pour les clés **reCAPTCHA Enterprise** (Google Cloud), le backend utilise l’API **`assessments`** dès que `gcp-project-id` et `gcp-api-key` sont renseignés dans `auth-service` (`application.yml`). Sinon il retombe sur `siteverify` + `secret-key` (souvent insuffisant pour un token Enterprise).

### Activer la verification Enterprise (obligatoire pour les cles « Score » GCP)

1. Google Cloud Console → **APIs et services** → **Bibliotheque** → activer **reCAPTCHA Enterprise API**.
2. **APIs et services** → **Identifiants** → **Creer des identifiants** → **Cle API** (vous pouvez restreindre la cle a l’API reCAPTCHA Enterprise).
3. Recuperer l’**ID de projet** GCP (pas seulement le nom affiche ; menu projet → **Parametres du projet**).
4. Dans `auth-service/src/main/resources/application.yml` :

```yaml
hirehub:
  recaptcha:
    site-key: "VOTRE_SITE_KEY"
    gcp-project-id: "votre-project-id-gcp"
    gcp-api-key: "votre-cle-api-google-cloud"
```

5. Redemarrer `auth-service`.

## Recruteur reste « en attente » / « verification en cours » dans l’admin

L’inscription enregistre le compte avec `PENDING_AUTO_CHECK`, puis **publie un message RabbitMQ**. Le microservice **`verification-service`** consomme ce message, lance l’OCR, puis renvoie **`RecruiterVerified`** ; **`auth-service`** met alors `recruiterApproved = true`.

**Si `verification-service` n’est pas démarré** (ou RabbitMQ est arrêté), l’événement n’est pas traité : le recruteur reste en **verification en cours** (badge bleu dans l’admin) jusqu’à ce que vous lanciez les services.

Ordre conseillé : **RabbitMQ** → **auth-service** → **verification-service** (ou au minimum RabbitMQ avant les deux).

## Validation stricte recruteur (oui/non immédiat)

Le flux d'inscription recruteur est désormais **bloquant** dans `auth-service` :

- OCR du justificatif pendant l'inscription
- comparaison avec `raisonSociale` et `siret` saisis
- si mismatch, rejet immédiat avec message clair

Codes d'erreur frontend:

- `justificatif_mismatch` : *Le justificatif ne correspond pas aux donnees du formulaire (raison sociale / SIRET).*
- `strict_verification_unavailable` : OCR indisponible (ex: `OCR_SPACE_API_KEY` manquante).

Configuration (`auth-service/src/main/resources/application.yml`):

- `hirehub.recruiter-verification.strict-enabled: true`
- `hirehub.recruiter-verification.ocr.api-key: ${OCR_SPACE_API_KEY:}`
- `hirehub.recruiter-verification.ocr.endpoint: ${OCR_SPACE_ENDPOINT:https://api.ocr.space/parse/image}`
- `hirehub.recruiter-verification.ocr.language: ${OCR_SPACE_LANGUAGE:fre}`

## Démarrage minimal pour test

## 1) Infrastructure

- PostgreSQL (port `55432`)
- RabbitMQ (port `5672`)

## 2) Services

- `auth-service` (port `8081`)
- `frontend-service` (port `8086`)

Eureka est optionnel pour ce test local simple.

## Scénarios à tester

## A) Inscription candidat

1. Ouvrir `http://localhost:8086/register/candidat`
2. Remplir le formulaire + reCAPTCHA
3. Envoyer
4. Vérifier redirection vers `login?registered=1`
5. Se connecter avec le compte créé

## B) Inscription recruteur

1. Ouvrir `http://localhost:8086/register/recruteur`
2. Remplir les champs + ajouter justificatif + reCAPTCHA
3. Envoyer
4. Vérifier redirection vers `login?registered=1`
5. Se connecter avec le compte recruteur

## C) Cas d'erreur reCAPTCHA

- Simuler token manquant/invalide (ou mauvaise clé)
- Vérifier redirection vers `login?registerError=1&code=recaptcha_failed`

## D) Vérifier l'admin

Compte par défaut (si non changé):

- email: `admin@hirehub.local`
- mot de passe: `Admin@12345`

Aller sur:

- `http://localhost:8086/admin/dashboard`
- `http://localhost:8086/admin/utilisateurs`

## Checklist rapide si login échoue

- le compte existe bien dans la table `users`
- le mot de passe est BCrypt ou correspond à l'ancien format (compatibilité temporaire)
- le champ `blocked` n'est pas `true`
- vous vous connectez bien sur `frontend-service` (`8086`), pas sur `auth-service` (`8081`)
