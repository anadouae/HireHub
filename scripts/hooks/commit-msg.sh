#!/bin/sh
# =============================================================================
# commit-msg
# Hook Git — exécuté automatiquement après la saisie du message de commit
#
# Rôle : valider que le message de commit respecte la convention
#        "Conventional Commits" (https://www.conventionalcommits.org)
#        avec les scopes adaptés à l'architecture ATS microservices.
#
# Pourquoi Conventional Commits ?
#   Un historique Git lisible est un outil de travail à part entière.
#   Cette convention permet de :
#     - Comprendre en un coup d'œil ce que fait chaque commit
#     - Générer automatiquement des changelogs
#     - Filtrer l'historique par service : git log --grep="candidate-service"
#     - Faciliter les revues de code et les bisect en cas de bug
#
# Format attendu :
#   type(scope): description courte (max 150 caractères)
#
#   Exemples valides :
#     feat(candidate-service): add CV upload endpoint
#     fix(api-gateway): correct routing for /auth/**
#     test(job-service): add unit tests for JobController
#     chore(deps): update Spring Boot to 3.2.1
#     ci: add GitHub Actions workflow for Docker build
# =============================================================================

# Le chemin vers le fichier contenant le message de commit est passé
# automatiquement par Git comme premier argument ($1) du script
COMMIT_MSG_FILE="$1"
COMMIT_MSG=$(cat "$COMMIT_MSG_FILE")


# -----------------------------------------------------------------------------
# CAS SPÉCIAUX — certains messages automatiques de Git doivent être ignorés
#
# Les merges ("Merge branch 'main' into feature/..."), rebases et squashes
# génèrent leurs propres messages — inutile de les valider.
# -----------------------------------------------------------------------------
if echo "$COMMIT_MSG" | grep -qE "^(Merge|Rebase|fixup!|squash!)"; then
  exit 0
fi

# On extrait uniquement la première ligne (le sujet du commit).
# Les lignes commençant par "#" sont des commentaires Git — on les ignore.
FIRST_LINE=$(echo "$COMMIT_MSG" | grep -v "^#" | head -1)


# -----------------------------------------------------------------------------
# DÉFINITION DES RÈGLES DE VALIDATION
# -----------------------------------------------------------------------------

# Types valides — chaque type a une signification précise :
#   feat     → nouvelle fonctionnalité visible par l'utilisateur
#   fix      → correction d'un bug
#   docs     → modifications de la documentation uniquement
#   style    → formatage, espaces, virgules (aucun changement de logique)
#   refactor → restructuration du code sans fix ni feature
#   test     → ajout ou correction de tests
#   chore    → tâches diverses : mise à jour de dépendances, config...
#   perf     → amélioration des performances
#   ci       → changements dans les pipelines CI/CD (GitHub Actions, etc.)
#   build    → changements dans le système de build (Maven, Docker, etc.)
#   revert   → annulation d'un commit précédent
TYPE_PATTERN="feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert"

# Scopes valides — correspondent aux modules de l'architecture ATS :
#   candidate-service    → gestion des candidats (CV, profil, candidature)
#   job-service          → gestion des offres d'emploi
#   notification-service → emails, services RabbitMQ
#   api-gateway          → Spring Cloud Gateway (routing, auth, rate limiting)
#   eureka-server        → service de découverte (Eureka)
#   config-server        → configuration centralisée (optionnel)
#   rabbitmq             → configuration et messages RabbitMQ
#   docker               → Dockerfile, docker-compose, images
#   ci                   → GitHub Actions, scripts de CI
#   deps                 → mise à jour de dépendances (pom.xml)
#   global               → changements qui touchent plusieurs services à la fois
VALID_SCOPES="candidate-service|job-service|notification-service|api-gateway|eureka-server|config-server|rabbitmq|docker|ci|deps|global"

# Pattern complet : type(scope optionnel): description de 1 à 150 caractères
FULL_PATTERN="^($TYPE_PATTERN)(\(($VALID_SCOPES)\))?: .{1,150}$"


# -----------------------------------------------------------------------------
# ÉTAPE 1 — Validation du format général
# -----------------------------------------------------------------------------
if ! echo "$FIRST_LINE" | grep -qE "$FULL_PATTERN"; then

  # Diagnostic précis : on identifie quelle partie du message est incorrecte
  # pour donner un message d'erreur utile plutôt qu'un simple "format invalide"

  if ! echo "$FIRST_LINE" | grep -qE "^($TYPE_PATTERN)"; then
    # Le type n'est pas reconnu (ex: "update:", "maj:", "added:")
    HINT="Type non reconnu. Types autorisés : feat, fix, docs, style, refactor, test, chore, perf, ci, build, revert"

  elif echo "$FIRST_LINE" | grep -qE "^($TYPE_PATTERN)\([^)]+\)" && \
       ! echo "$FIRST_LINE" | grep -qE "^($TYPE_PATTERN)\(($VALID_SCOPES)\)"; then
    # Le scope existe mais ne correspond pas à un module connu
    HINT="Scope non reconnu. Scopes autorisés : candidate-service, job-service, notification-service, api-gateway, eureka-server, config-server, rabbitmq, docker, ci, deps, global"

  elif ! echo "$FIRST_LINE" | grep -qE ": .+"; then
    # Pas de description après les deux-points
    HINT="Description manquante après ':'. Format : type(scope): description"

  else
    HINT="Format invalide. Vérifie la structure complète ci-dessous."
  fi

  echo ""
  echo "❌  Message de commit invalide :"
  echo "   \"$FIRST_LINE\""
  echo ""
  echo "   $HINT"
  echo ""
  echo "   Format attendu : type(scope): description"
  echo "   Le scope est optionnel mais fortement recommandé dans ce projet."
  echo ""
  echo "   Exemples pour l'ATS :"
  echo "     feat(candidate-service): add CV upload endpoint"
  echo "     fix(api-gateway): correct routing for /auth/**"
  echo "     test(job-service): add unit tests for JobController"
  echo "     chore(deps): update Spring Boot to 3.2.1"
  echo "     refactor(notification-service): extract email template logic"
  echo "     ci: add GitHub Actions workflow for Docker build"
  echo "     docs: add API contract for Phase 0 endpoints"
  echo ""
  exit 1   # Bloque le commit
fi


# -----------------------------------------------------------------------------
# ÉTAPE 2 — Vérification de la qualité de la description
#
# Un message comme "fix(candidate-service): fix" est techniquement valide
# selon le pattern regex, mais inutile en pratique.
# On détecte les descriptions trop vagues et on les refuse.
# -----------------------------------------------------------------------------
VAGUE_DESCRIPTIONS="^(fix|update|change|modify|edit|wip|temp|test|misc|stuff|things|correction|modification)$"

# Extraire uniquement la description (ce qui suit ": ")
DESCRIPTION=$(echo "$FIRST_LINE" | sed 's/^[^:]*: //')

if echo "$DESCRIPTION" | grep -qiE "$VAGUE_DESCRIPTIONS"; then
  echo ""
  echo "⚠️   Description trop vague :"
  echo "   \"$FIRST_LINE\""
  echo ""
  echo "   Décris ce que fait le commit concrètement. Exemples :"
  echo "   ✗  fix(candidate-service): fix"
  echo "   ✓  fix(candidate-service): handle null pointer in CandidateMapper"
  echo ""
  echo "   ✗  feat(job-service): update"
  echo "   ✓  feat(job-service): add pagination to job listing endpoint"
  echo ""
  exit 1
fi

exit 0   # Message valide → Git autorise le commit