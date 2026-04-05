#!/bin/sh
# =============================================================================
# run-checkstyle.sh
# Hook Git — exécuté automatiquement au moment du "git commit" (stage: commit)
#
# Rôle : vérifier que les fichiers Java stagés respectent les règles de style
#        définies dans la config Checkstyle du projet (indentation, nommage,
#        longueur des lignes, Javadoc...).
#
# Pourquoi au commit et pas au push ?
#   Le style doit être corrigé avant d'entrer dans l'historique Git. Une
#   violation de style détectée au commit coûte 30 secondes à corriger ;
#   détectée après un push et une PR, elle coûte une révision complète.
#
# Mécanique :
#   1. Lister les fichiers Java stagés (ajoutés ou modifiés)
#   2. Extraire les services concernés
#   3. Pour chaque service : mvn checkstyle:check -pl <service>
# =============================================================================


# -----------------------------------------------------------------------------
# ÉTAPE 1 — Vérifier s'il y a des fichiers Java dans le staging
#
# "--diff-filter=ACM" : on ne vérifie que les fichiers Added, Copied ou Modified.
# On ignore les fichiers Deleted (D) — Checkstyle ne peut pas analyser
# un fichier qui n'existe plus.
# -----------------------------------------------------------------------------
STAGED_JAVA=$(git diff --cached --name-only --diff-filter=ACM | grep "\.java$")

# Aucun fichier Java stagé → rien à vérifier, on laisse passer
if [ -z "$STAGED_JAVA" ]; then
  exit 0
fi

# Extraire les noms de services uniques à partir des chemins de fichiers
# (même logique que dans maven-compile.sh)
CHANGED_SERVICES=$(echo "$STAGED_JAVA" | cut -d'/' -f1 | sort -u)

echo ""
echo "🔍  Checkstyle — vérification des services modifiés..."
echo ""

FAILED=0   # Compteur d'échecs


# -----------------------------------------------------------------------------
# ÉTAPE 2 — Lancer Checkstyle service par service
# -----------------------------------------------------------------------------
for SERVICE in $CHANGED_SERVICES; do

  # Ignorer les dossiers sans pom.xml (scripts, docs, etc.)
  if [ ! -f "$SERVICE/pom.xml" ]; then
    echo "   ⏭️   $SERVICE — pas de pom.xml, ignoré"
    continue
  fi

  echo "   → Vérification de : $SERVICE"

  # "-q" (quiet) : Maven n'affiche que les violations, pas le build complet.
  # Le rapport XML complet est généré dans target/checkstyle-result.xml.
  mvn checkstyle:check -q -pl "$SERVICE" 2>&1

  if [ $? -ne 0 ]; then
    echo ""
    echo "   ❌  Violations Checkstyle dans : $SERVICE"
    echo "       Rapport détaillé : $SERVICE/target/checkstyle-result.xml"
    echo "       Tu peux aussi corriger automatiquement avec :"
    echo "       mvn checkstyle:check -pl $SERVICE"
    echo ""
    FAILED=1
    # On continue pour analyser tous les services et avoir un rapport complet.
  fi

done


# -----------------------------------------------------------------------------
# ÉTAPE 3 — Bilan et décision finale
# -----------------------------------------------------------------------------
if [ $FAILED -ne 0 ]; then
  echo "❌  Commit bloqué — corrige les violations de style avant de committer."
  exit 1   # Code de sortie non-nul = Git annule le commit
fi

echo "✅  Checkstyle OK — aucune violation détectée."
echo ""
exit 0   # Code de sortie 0 = Git autorise le commit