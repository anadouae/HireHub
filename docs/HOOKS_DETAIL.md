# 📊 Tableau Comparatif des 5 Hooks Git

## Vue d'ensemble

| # | Hook | Fichier source | Stage | Rôle | Bloque ? | Temps |
|---|------|-----------------|-------|------|---------|-------|
| 1 | `commit-msg` | `commit-msg.sh` | Commit | Valide format message | ✅ Oui | <1s |
| 2 | `pre-commit` | `check-sensitive-files.sh` + `run-checkstyle.sh` | Commit | Sécurité + Style code | ✅ Oui | 5-10s |
| 3 | `pre-push` | `maven-compile.sh` + `maven-test.sh` | Push | Compile + Teste | ✅ Oui | 30-60s |

---

## 🎯 Détail de chaque hook

### 1️⃣ commit-msg Hook

```
QUAND : git commit -m "message"
OÙ : .git/hooks/commit-msg
SOURCE : scripts/hooks/commit-msg.sh

VALIDATION :
├─ Format valide ? → type(scope): description
├─ Type reconnu ? → feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert
├─ Scope reconnu ? → candidate-service|job-service|notification-service|...
├─ Description courte ? ≤ 72 caractères
└─ Description précise ? (pas trop vague : "fix", "update", "misc"...)

RÉSULTATS :
✓ Message valide → Commit accepté
✗ Message invalide → Diagnostic précis + Exemples → Commit bloqué

BYPASS : git commit --no-verify

TEMPS : <1 seconde
```

---

### 2️⃣ pre-commit Hook (Part A : check-sensitive-files)

```
QUAND : git commit -m "message"
OÙ : .git/hooks/pre-commit (première partie)
SOURCE : scripts/hooks/check-sensitive-files.sh

PHASE 1 - VÉRIFICATION STRICTE :
├─ Fichiers interdits détectés ?
│  ├─ .env, .env.local, .env.prod
│  ├─ application-local.*, application-secret.*
│  ├─ docker-compose.override.*, docker-compose.prod.*
│  ├─ .jks, .pfx, .p12 (keystores)
│  └─ credentials.json, service-account.json
│
└─ SI OUI → Commit bloqué + Message d'aide

PHASE 2 - VÉRIFICATION SOUPLE :
├─ Secrets détectés dans le code ?
│  ├─ password = ...
│  ├─ api_key: ...
│  ├─ token: ...
│  └─ private_key: ...
│
└─ SI OUI → Avertissement affiché (commit autorisé)

BYPASS : git commit --no-verify

TEMPS : 1-2 secondes
```

---

### 3️⃣ pre-commit Hook (Part B : run-checkstyle)

```
QUAND : git commit -m "message"
OÙ : .git/hooks/pre-commit (deuxième partie)
SOURCE : scripts/hooks/run-checkstyle.sh

PROCESSUS :
1. Lister les fichiers Java stagés
2. Extraire les services modifiés
3. Pour chaque service :
   └─ Exécuter : mvn checkstyle:check -q -pl <service>

VÉRIFICATIONS (Checkstyle) :
├─ Indentation (espaces/tabs)
├─ Nommage des variables (camelCase, conventions)
├─ Nommage des classes (PascalCase)
├─ Longueur des lignes
├─ Javadoc présente et valide
├─ Imports organises
├─ Pas d'espaces en fin de ligne
└─ Et 50+ autres règles de style

RÉSULTATS :
✓ Pas de violations → Commit accepté
✗ Violations trouvées :
  └─ Rapport : <service>/target/checkstyle-result.xml
  └─ Commit bloqué

BYPASS : git commit --no-verify

TEMPS : 3-10 secondes (dépend du nombre de services modifiés)
```

---

### 4️⃣ pre-push Hook (Part A : maven-compile)

```
QUAND : git push origin <branch>
OÙ : .git/hooks/pre-push (première partie)
SOURCE : scripts/hooks/maven-compile.sh

PROCESSUS :
1. Comparer HEAD avec remote : quels fichiers Java ont changé ?
2. Extraire les noms des services depuis les chemins
3. Pour chaque service modifié :
   └─ Exécuter : mvn compile -q -pl <service> -am

DÉTAIL :
-q           = quiet mode (affiche que les erreurs)
-pl <service> = compile ce module uniquement
-am          = also make (compile aussi les dépendances parentes)

RÉSULTATS :
✓ Tous compilent → Push accepté
✗ Au moins un échoue :
  └─ Message d'erreur de Maven
  └─ Push bloqué

BYPASS : git push --no-verify

TEMPS : 10-30 secondes (dépend du nombre/taille des services)

AVANTAGE : Évite de recompiler le monorepo entier.
```

---

### 5️⃣ pre-push Hook (Part B : maven-test)

```
QUAND : git push origin <branch>
OÙ : .git/hooks/pre-push (deuxième partie)
SOURCE : scripts/hooks/maven-test.sh

PROCESSUS :
1. Déterminer la branche remote (git rev-parse)
2. Diff HEAD vs remote = fichiers Java modifiés
3. Extraire les noms des services
4. Pour chaque service modifié (qui a des tests) :
   └─ Exécuter : mvn test -pl <service> -am -Dtest="**/*Test,**/*Tests,**/*Spec"

FILTRES :
├─ Ignore les services sans pom.xml
├─ Ignore les services sans répertoire src/test/java
├─ Ignore les services sans classes *Test.java
└─ Résultat : ne teste que les services pertinents

RÉSULTATS :
✓ Tous les tests passent → Push accepté
✗ Au moins un échoue :
  └─ Rapport : <service>/target/surefire-reports/
  └─ Push bloqué

BYPASS : git push --no-verify

TEMPS : 15-60 secondes (dépend du nombre/complexité des tests)

CONVENTIONS DE NOMMAGE DÉTECTÉES :
├─ *Test.java
├─ *Tests.java
└─ *Spec.java
```

---

## 📈 Timeline d'un commit & push

```
$ git commit -m "feat(candidate-service): add CV upload"

┌─────────────────────────────────────┐
│ Hook 1: commit-msg                  │ <1s
│ ✓ Format Conventional Commits OK    │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│ Hook 2a: check-sensitive-files      │ 1-2s
│ ✓ Pas de fichiers sensibles         │
│ ✓ Pas de secrets detectes          │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│ Hook 2b: run-checkstyle             │ 3-10s
│ ✓ Style Java OK                     │
└─────────────────────────────────────┘
           ↓
✅ COMMIT REUSSI (5-13s total)

===== PLUS TARD =====

$ git push origin feature/cv-upload

┌─────────────────────────────────────┐
│ Hook 4: maven-compile               │ 10-30s
│ ✓ Services modifiés compilent OK    │
└─────────────────────────────────────┘
           ↓
┌─────────────────────────────────────┐
│ Hook 5: maven-test                  │ 15-60s
│ ✓ Tests unitaires OK                │
└─────────────────────────────────────┘
           ↓
✅ PUSH REUSSI (25-90s total)
```

---

## 🔴 Scénarios d'échec

### Commit échoue : message invalide
```
❌ Message de commit invalide :
   "update stuff"

   Description trop vague. Format : type(scope): description

   Exemples valides :
     feat(candidate-service): add CV upload endpoint
     fix(api-gateway): correct routing for /auth/**

$ git commit --amend -m "feat(candidate-service): add CV upload endpoint"
✅ Commit réussi
```

### Commit échoue : fichier sensible détecté
```
❌ Fichiers sensibles détectés dans le commit :
   → .env
   → docker-compose.override.yml

   Ces fichiers ne doivent jamais entrer dans l'historique Git.

$ git restore --staged .env
$ git restore --staged docker-compose.override.yml
$ echo ".env" >> .gitignore
$ echo "docker-compose.override.yml" >> .gitignore
$ git commit -m "chore: add .env to gitignore"
✅ Commit réussi
```

### Commit échoue : violations Checkstyle
```
❌ Violations Checkstyle dans : candidate-service
   Rapport détaillé : candidate-service/target/checkstyle-result.xml

   Exemples de violations :
   - Line exceeds 120 characters
   - Missing Javadoc for public method
   - Unused import statement

# Corriger le code, puis :
$ git add src/main/java/MyClass.java
$ git commit -m "style: fix Checkstyle violations in candidate-service"
✅ Commit réussi
```

### Push échoue : compilation error
```
❌ Échec dans : candidate-service
   Corrige les erreurs de compilation avant de push.

   Erreur détectée :
   [ERROR] error: symbol not found in class CandidateMapper

# Corriger le code :
$ ./mvnw compile -pl candidate-service
$ git add src/main/java/MyClass.java
$ git commit -m "fix(candidate-service): fix NullPointerException in mapper"
$ git push origin feature/my-feature
✅ Push réussi
```

### Push échoue : test failure
```
❌ Tests échoués dans : candidate-service
   Consulte le rapport : candidate-service/target/surefire-reports/

   Tests échoues :
   CandidateControllerTest.testUploadCV FAILED
   Assertion failed: expected true but was false

# Corriger le code ou les tests :
$ ./mvnw test -pl candidate-service
$ git add src/main/java/MyClass.java
$ git commit -m "test(candidate-service): fix CV upload test"
$ git push origin feature/my-feature
✅ Push réussi
```

---

## 📋 Checklist avant commit/push

### ✅ Avant de committer

- [ ] Code compilé localement (`./mvnw compile`)
- [ ] Style Java respecté (lancer Checkstyle manuellement si besoin)
- [ ] Message au format Conventional Commits
- [ ] Pas de fichiers sensibles dans le staging
- [ ] Pas de secrets hardcodés

### ✅ Avant de pusher

- [ ] Tous les tests passent localement (`./mvnw test`)
- [ ] Code compilé sans erreurs
- [ ] Branche à jour avec `main` (sinon merge conflicts)
- [ ] Commit forcé ? Non ! (sauf urgence extrême)

---

## 🛠️ Commandes utiles

```bash
# Voir les fichiers stagés
git diff --cached --name-only

# Voir les changements stagés
git diff --cached

# Vérifier style manuellement
./mvnw checkstyle:check -pl candidate-service

# Lancer tests manuellement
./mvnw test -pl candidate-service

# Voir les hooks installés
ls -la .git/hooks/

# Contourner tous les hooks
git commit --no-verify
git push --no-verify

# Désactiver les hooks globalement
git config core.hooksPath /dev/null

# Réactiver les hooks
git config --unset core.hooksPath
```

---

## 💡 Bonnes pratiques

1. **Commitez souvent** : petits commits logiques sont mieux que gros commits
2. **Messages explicites** : "fix(service): <quoi> et <pourquoi>"
3. **Une branche = une feature** : créez une branche pour chaque feature/fix
4. **Rebase avant push** : `git rebase main` pour éviter les merge commits
5. **Testez avant push** : `./mvnw test` localement
6. **Relisez votre diff** : `git diff --cached` avant commit
7. **Ne forcez pas** : `git push --force` = danger en équipe

---

## 🎓 Apprentissage progressif

### Jour 1 : Les bases
- Apprendre le format Conventional Commits
- Faire des commits avec messages corrects
- Les hooks vont bloquer si mauvais format → apprendre rapidement !

### Jour 2-3 : Style du code
- Respecter Checkstyle automatiquement
- Lire les rapports Checkstyle
- Corriger les violations

### Jour 4-7 : Tests & Compilation
- Lancer tests avant push
- Comprendre les echecs Maven
- Debugger les tests

### Semaine 2+ : Maîtrise
- Optimiser le workflow (rebase, squash)
- Relire l'historique Git
- Aider d'autres développeurs


