#!/bin/sh
# =============================================================================
# check-sensitive-files.sh
# Hook Git — exécuté automatiquement au moment du "git commit" (stage: commit)
#
# Rôle : empêcher les fichiers de configuration sensibles (mots de passe,
#        clés privées, tokens, certificats...) d'entrer dans l'historique Git.
#
# Pourquoi est-ce critique ?
#   Une fois qu'un secret est commité, même si on le supprime ensuite,
#   il reste accessible dans l'historique Git pour toujours (git log, git show).
#   Si le repo est public ou partagé, le secret est compromis — il faut
#   impérativement le révoquer et en générer un nouveau.
#
# Ce hook fait deux vérifications :
#   1. VÉRIFICATION STRICTE  — bloque les fichiers dont le nom correspond
#      à des patterns connus de fichiers sensibles (.env, .jks, *-local.*, etc.)
#   2. VÉRIFICATION SOUPLE   — affiche un avertissement si une valeur qui
#      ressemble à un secret est détectée directement dans le code (warning
#      uniquement, ne bloque pas — pour éviter les faux positifs)
# =============================================================================


# -----------------------------------------------------------------------------
# ÉTAPE 1 — Définir les patterns de fichiers interdits
#
# Ces patterns couvrent les cas les plus courants dans un projet Spring Boot :
#   - .env, .env.local, .env.prod     → variables d'environnement
#   - application-local.*             → config Spring locale (DB locale, etc.)
#   - application-secret.*            → config contenant des secrets
#   - application-prod.*              → config de production
#   - docker-compose.override.*       → surcharges Docker locales
#   - docker-compose.prod.*           → config Docker de production
#   - .p12, .jks, .pfx               → keystores et certificats
#   - secrets.yml / secrets.props     → fichiers de secrets explicites
#   - credentials.json                → credentials Google/Firebase/etc.
#   - service-account.json            → comptes de service cloud
# -----------------------------------------------------------------------------
FORBIDDEN_PATTERNS="\
\\.env$|\
\\.env\\.local$|\
\\.env\\.prod$|\
application-local\\.properties$|\
application-local\\.yml$|\
application-secret\\.properties$|\
application-secret\\.yml$|\
application-prod\\.properties$|\
application-prod\\.yml$|\
docker-compose\\.override\\.yml$|\
docker-compose\\.prod\\.yml$|\
\\.p12$|\
\\.jks$|\
\\.pfx$|\
secrets\\.yml$|\
secrets\\.properties$|\
credentials\\.json$|\
service-account\\.json$"


# -----------------------------------------------------------------------------
# ÉTAPE 2 — Vérification stricte : noms de fichiers interdits
#
# "git diff --cached --name-only" liste tous les fichiers dans le staging area.
# On filtre avec grep selon nos patterns pour voir si l'un d'eux est interdit.
# -----------------------------------------------------------------------------
STAGED=$(git diff --cached --name-only)
BLOCKED=$(echo "$STAGED" | grep -E "$FORBIDDEN_PATTERNS")

if [ -n "$BLOCKED" ]; then
  echo ""
  echo "❌  Fichiers sensibles détectés dans le commit :"
  echo ""
  # Affiche chaque fichier bloqué sur une ligne séparée
  echo "$BLOCKED" | while read f; do
    echo "     → $f"
  done
  echo ""
  echo "   Ces fichiers ne doivent jamais entrer dans l'historique Git."
  echo ""
  echo "   Pour corriger :"
  echo "   1. Retire le fichier du staging :"
  echo "      git restore --staged <fichier>"
  echo ""
  echo "   2. Ajoute-le au .gitignore pour ne plus y penser :"
  echo "      echo '<fichier>' >> .gitignore"
  echo ""
  echo "   3. Si tu l'as déjà commité par erreur dans le passé :"
  echo "      git rm --cached <fichier>"
  echo "      → Révoque IMMÉDIATEMENT les credentials exposés (token, password...)"
  echo "        car ils restent visibles dans git log même après suppression."
  echo ""
  exit 1   # Bloque le commit
fi


# -----------------------------------------------------------------------------
# ÉTAPE 3 — Vérification souple : secrets hardcodés dans le code source
#
# On inspecte le contenu des lignes ajoutées (lignes commençant par "+")
# dans les fichiers de code et de config stagés. On cherche des patterns
# du type "password=abc123def" ou "api_key: xK9zP2..." dans le diff.
#
# On exclut les valeurs clairement fictives : placeholders, variables
# d'environnement Spring (${...}), commentaires (#), valeurs de test...
#
# Résultat : warning affiché mais commit non bloqué (exit 0).
# Passe en exit 1 pour durcir ce comportement si l'équipe le souhaite.
# -----------------------------------------------------------------------------
STAGED_SOURCE_FILES=$(git diff --cached --name-only --diff-filter=ACM \
  | grep -E "\.(java|properties|yml|yaml|xml|json)$")

if [ -n "$STAGED_SOURCE_FILES" ]; then

  SUSPECT=$(git diff --cached -- $STAGED_SOURCE_FILES \
    | grep "^+" \
    | grep -iE "(password|secret|api[_-]?key|token|private[_-]?key)\s*[=:]\s*['\"]?[a-zA-Z0-9+/=_\-]{8,}" \
    | grep -vE "(your_|changeme|example|placeholder|dummy|test|TODO|xxx|\$\{|#)")

  if [ -n "$SUSPECT" ]; then
    echo ""
    echo "⚠️   Valeurs potentiellement sensibles détectées dans le code :"
    echo ""
    # On affiche au maximum 5 lignes suspectes pour ne pas noyer l'output
    echo "$SUSPECT" | head -5 | while read line; do
      echo "     $line"
    done
    echo ""
    echo "   Bonne pratique Spring Boot : utilise des variables d'environnement"
    echo "   plutôt que des valeurs hardcodées dans tes properties :"
    echo "     spring.datasource.password=\${DB_PASSWORD}"
    echo "   Et stocke les vraies valeurs dans GitHub Secrets pour le CI/CD."
    echo ""
    echo "   Si c'est un faux positif, tu peux passer outre avec :"
    echo "   git commit --no-verify"
    echo ""
    # exit 0 → warning uniquement, le commit est autorisé
    # Passe en exit 1 si tu veux bloquer strictement
  fi

fi

exit 0