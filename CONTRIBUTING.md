# Contribuer à HireHub

## 📦 Installation initiale

1. Forkez ou clonez [HireHub---PROJET-JEE](https://github.com/anadouae/HireHub---PROJET-JEE).
2. Créez une branche depuis `main`.
3. Installez **JDK 17** et définissez **`JAVA_HOME`** (obligatoire pour `mvnw` sous Windows).

## 🔧 Configuration des hooks Git

Les hooks Git automatisent les vérifications (commit format, style du code, tests, compilation) avant chaque commit et push.

**Installation des hooks :**

- **Windows (PowerShell)** :
  ```powershell
  powershell -ExecutionPolicy Bypass -File scripts/install-hooks.ps1
  ```

- **Linux / macOS** :
  ```bash
  chmod +x scripts/install-hooks.sh
  ./scripts/install-hooks.sh
  ```

**Vérification de l'installation :**
```bash
ls -la .git/hooks/  # Linux/macOS
dir .git\hooks\     # Windows
```

Vous devez voir les fichiers : `pre-commit`, `commit-msg`, `pre-push`

## 📝 Compilation et tests

À la racine, compilez avec le **Maven Wrapper** (aucune installation Maven globale requise) :
   - Windows : `.\mvnw.cmd -q -DskipTests package`
   - Linux / macOS : `./mvnw -q -DskipTests package`

## 📤 Commits et Pull Requests

Les hooks Git appliquent automatiquement les règles suivantes avant chaque commit et push :

### Format des commits (Conventional Commits)
```
type(scope): description courte (max 72 caractères)
```

**Types :** `feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`, `perf`, `ci`, `build`, `revert`

**Scopes** (optionnel mais recommandé) : `candidate-service`, `job-service`, `notification-service`, `api-gateway`, `eureka-server`, `config-server`, `rabbitmq`, `docker`, `ci`, `deps`, `global`

**Exemples :**
- ✅ `feat(candidate-service): add CV upload endpoint`
- ✅ `fix(api-gateway): correct routing for /auth/**`
- ✅ `test(job-service): add unit tests for JobController`
- ✅ `chore(deps): update Spring Boot to 3.2.1`

### Vérifications automatiques

| Stage | Vérification | Action |
|-------|-------------|--------|
| **Commit** | Format du message | Bloque si invalide |
| **Commit** | Fichiers sensibles (.env, .jks, credentials.json...) | Bloque les secrets |
| **Commit** | Style du code (Checkstyle) | Bloque les violations |
| **Push** | Compilation Maven | Bloque si erreurs |
| **Push** | Tests unitaires | Bloque si tests échouent |

### Contourner les hooks en urgence

```bash
git commit --no-verify     # Bypasse pre-commit et commit-msg
git push --no-verify       # Bypasse pre-push
```

## 📋 Avant d'ouvrir une Pull Request

1. Créez une branche depuis `main` avec un nom explicite : `feature/cv-upload` ou `fix/gateway-routing`
2. Commitez régulièrement avec des messages clairs
3. Lancez les tests localement : `./mvnw test`
4. Pushez et ouvrez une **pull request** avec une description claire (module touché, comportement, captures si UI)

Voir aussi `docs/COLLABORATION.md` et `docs/ARCHITECTURE.md`.
