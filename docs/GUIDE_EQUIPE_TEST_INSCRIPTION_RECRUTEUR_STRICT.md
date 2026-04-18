# Guide equipe - Pull et tests complets (inscription recruteur stricte)

Ce document explique exactement comment recuperer les changements, lancer l'application et tester tout le flux "oui/non immediat" pour l'inscription recruteur.

## 1) Objectif du lot

Le comportement implemente est:

- plus de verrouillage "en attente" pour les nouveaux comptes recruteur;
- decision immediate a l'inscription:
  - **OK** si formulaire + justificatif coherent (OCR + comparaison);
  - **KO** si incoherence (raison sociale / SIRET) ou OCR indisponible;
- message clair cote UI en cas d'erreur.

## 2) Contenu principal implemente

- Validation stricte OCR ajoutee dans `auth-service`:
  - `auth-service/src/main/java/com/hirehub/auth/service/RecruiterDocumentStrictValidationService.java`
- Controle branche dans le endpoint:
  - `auth-service/src/main/java/com/hirehub/auth/web/AuthController.java`
- Compte recruteur cree directement approuve:
  - `auth-service/src/main/java/com/hirehub/auth/service/UserRegistrationService.java`
- Message d'erreur frontend (login) enrichi:
  - `frontend-service/src/main/resources/templates/pages/public/login.html`
- Configuration OCR stricte:
  - `auth-service/src/main/resources/application.yml`

## 3) Recuperer les changements (depuis votre machine)

Depuis la racine du projet:

```powershell
git pull
```

Verifier rapidement les fichiers recuperes:

```powershell
git log --oneline -n 10
git status
```

## 4) Prerequis de test

- Java 17
- Maven wrapper du projet (`mvnw.cmd`)
- Docker (si vous utilisez PostgreSQL local du `docker-compose.yml`)
- Base auth accessible selon `application.yml` (`jdbc:postgresql://localhost:55432/hirehub_auth`)

## 5) Configuration OCR (etat actuel du projet)

La cle OCR est actuellement en dur dans:

- `auth-service/src/main/resources/application.yml`
  - `hirehub.recruiter-verification.ocr.api-key: "K87844108488957"`

Ce choix est pratique pour la demo/prof, mais a retirer avant publication externe.

## 6) Demarrage minimal pour ces tests

### 6.1 Infra (si necessaire)

```powershell
docker compose up -d
```

### 6.2 Lancer le frontend

Dans un terminal, a la racine:

```powershell
.\mvnw.cmd -pl frontend-service -am spring-boot:run
```

### 6.3 Lancer auth-service

Dans un second terminal:

```powershell
.\mvnw.cmd -pl auth-service -am spring-boot:run
```

Notes:

- avec le nouveau flux strict, `verification-service` n'est plus necessaire pour decider l'acceptation/rejet a l'inscription;
- RabbitMQ n'est pas requis pour cette decision immediate.

## 7) URL utiles

- Frontend: `http://localhost:8086`
- Formulaire inscription recruteur: `http://localhost:8086/register/recruteur`
- Login (message resultat): `http://localhost:8086/login`
- Admin utilisateurs: `http://localhost:8086/admin/utilisateurs`

## 8) Plan de tests (a faire dans cet ordre)

## Test A - Cas nominal (doit passer)

1. Ouvrir `/register/recruteur`.
2. Saisir:
   - nom complet valide
   - email jamais utilise
   - raison sociale coherente avec le document
   - SIRET coherent avec le document
   - presentation >= 20 caracteres
   - fichier justificatif lisible (PDF/JPG/PNG)
3. Soumettre.

Resultat attendu:

- redirection vers `/login?registered=1`;
- message: inscription reussie;
- compte recruteur connectable;
- dans admin utilisateurs: statut actif (pas "en attente").

## Test B - Mismatch raison sociale / SIRET (doit echouer)

1. Reprendre un justificatif d'une entreprise X.
2. Dans le formulaire, saisir une raison sociale Y (ou un SIRET different).
3. Soumettre.

Resultat attendu:

- redirection vers `/login?registerError=1&code=justificatif_mismatch`;
- message affiche:
  - "Le justificatif ne correspond pas aux donnees du formulaire (raison sociale / SIRET)."
- aucun compte recruteur valide ne doit etre cree.

## Test C - Fichier non supporte (doit echouer)

1. Uploader un format non supporte (ex: `.txt`).
2. Soumettre.

Resultat attendu:

- rejet immediat;
- message de format non supporte.

## Test D - OCR indisponible (doit echouer avec message dedie)

Precondition:

- vider la cle OCR dans `application.yml` (`api-key: ""`) ou la rendre invalide.

1. Redemarrer `auth-service`.
2. Soumettre une inscription recruteur.

Resultat attendu:

- redirection `/login?registerError=1&code=strict_verification_unavailable`;
- message:
  - "Verification OCR indisponible pour le moment. Reessayez plus tard."

## Test E - Email deja utilise (doit echouer)

1. Reutiliser un email recruteur deja en base.
2. Soumettre.

Resultat attendu:

- redirection `/login?registerError=1&code=email_taken`;
- message:
  - "Cet email est deja utilise."

## 9) Verification technique optionnelle (API)

Vous pouvez aussi verifier via API:

- Endpoint: `POST /api/v1/auth/register/recruteur`
- En `multipart/form-data`

Cas mismatch attendu:

- HTTP `400` + payload d'erreur avec message explicite.

Cas OCR indisponible:

- HTTP `503` + payload d'erreur.

## 10) Fichiers a relire en revue equipe

- `auth-service/src/main/java/com/hirehub/auth/service/RecruiterDocumentStrictValidationService.java`
- `auth-service/src/main/java/com/hirehub/auth/web/AuthController.java`
- `auth-service/src/main/java/com/hirehub/auth/service/UserRegistrationService.java`
- `frontend-service/src/main/resources/templates/pages/public/login.html`
- `auth-service/src/main/resources/application.yml`

## 11) Depannage rapide

- **Toujours "verification indisponible"**
  - verifier la cle OCR;
  - verifier que `auth-service` a bien ete redemarre.
- **Erreur OCR sur document**
  - essayer un PDF/JPG/PNG plus net;
  - eviter photos floues.
- **Redirection login sans bon message**
  - verifier le parametre `code` dans l'URL;
  - verifier le switch des erreurs dans `login.html`.

## 12) Important securite (avant publication GitHub)

Avant push public:

- enlever la cle OCR en dur de `application.yml`;
- remettre:

```yaml
api-key: ${OCR_SPACE_API_KEY:}
```

- stocker la vraie cle uniquement en variable d'environnement / secret manager.

---

Document prepare pour faciliter la reprise par toute l'equipe (dev, QA, demo prof).
