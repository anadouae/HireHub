#!/bin/sh
# =============================================================================
# maven-test.sh
# Hook Git — exécuté automatiquement au moment du "git push" (stage: push)
#
# Rôle : lancer les tests unitaires uniquement pour les services modifiés
#        depuis le dernier push, afin d'éviter de tout retester à chaque fois.
#
# Pourquoi au push et pas au commit ?
#   Les tests prennent plusieurs secondes par service. Les lancer à chaque
#   "git commit" ralentirait trop le workflow quotidien. Le push est le bon
#   moment : c'est là qu'on partage son code avec l'équipe.
#
# Mécanique :
#   1. Comparer HEAD avec le remote pour lister les services touchés
#   2. Ignorer les modules sans tests (eureka-server, config-server...)
#   3. Pour chaque service : mvn test -pl <service> -am
# =============================================================================


# -----------------------------------------------------------------------------
# ÉTAPE 1 — Détection des services modifiés depuis le dernier push
#
# On cherche la branche remote associée à la branche locale courante.
# Si elle existe (cas normal après le premier push), on diff HEAD vs remote
# pour voir tous les fichiers Java modifiés dans les commits à pousser.
# Si elle n'existe pas encore (premier push d'une nouvelle branche), on
# se rabat sur les fichiers stagés.
# -----------------------------------------------------------------------------
REMOTE_BRANCH=$(git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null)

if [ -n "$REMOTE_BRANCH" ]; then
  # Cas normal : la branche remote existe
  # "..." (3 points) = diff symétrique : tous les commits présents dans HEAD
  # mais pas encore dans le remote, et vice-versa
  CHANGED_SERVICES=$(git diff --name-only "$REMOTE_BRANCH"...HEAD \
    | grep "\.java$" \
    | cut -d'/' -f1 \
    | sort -u)
else
  # Premier push d'une nouvelle branche : pas de remote encore
  # On utilise les fichiers stagés comme approximation
  CHANGED_SERVICES=$(git diff --cached --name-only \
    | grep "\.java$" \
    | cut -d'/' -f1 \
    | sort -u)
fi

# Aucun fichier Java modifié → rien à tester, on laisse passer
if [ -z "$CHANGED_SERVICES" ]; then
  echo "ℹ️   Aucun fichier Java modifié — tests ignorés."
  exit 0
fi

echo ""
echo "🧪  Tests unitaires Maven — services modifiés détectés..."
echo ""

FAILED=0    # Compteur d'échecs
TESTED=0    # Compteur de services effectivement testés


# -----------------------------------------------------------------------------
# ÉTAPE 2 — Lancement des tests service par service
# -----------------------------------------------------------------------------
for SERVICE in $CHANGED_SERVICES; do

  # Ignorer les dossiers qui ne sont pas des modules Maven
  if [ ! -f "$SERVICE/pom.xml" ]; then
    echo "   ⏭️   $SERVICE — pas de pom.xml, ignoré"
    continue
  fi

  # Ignorer les modules qui n'ont pas de tests unitaires.
  # C'est courant pour eureka-server ou config-server : ce sont des modules
  # de configuration pure, sans logique métier à tester.
  TEST_DIR="$SERVICE/src/test/java"
  if [ ! -d "$TEST_DIR" ] || [ -z "$(find "$TEST_DIR" -name '*Test.java' 2>/dev/null)" ]; then
    echo "   ⏭️   $SERVICE — aucun test trouvé, ignoré"
    continue
  fi

  echo "   → Tests de : $SERVICE"
  TESTED=$((TESTED + 1))

  # -Dtest="**/*Test,**/*Tests,**/*Spec" : convention de nommage standard
  #   pour les classes de test (JUnit, Mockito, Spring Boot Test)
  # -DfailIfNoTests=false : ne pas planter si un module n'a aucun test
  #   correspondant au pattern (évite les faux positifs)
  mvn test -q \
    -pl "$SERVICE" -am \
    -Dtest="**/*Test,**/*Tests,**/*Spec" \
    -DfailIfNoTests=false \
    2>&1

  if [ $? -ne 0 ]; then
    echo ""
    echo "   ❌  Tests échoués dans : $SERVICE"
    echo "       Consulte le rapport : $SERVICE/target/surefire-reports/"
    echo ""
    FAILED=1
    # On continue la boucle pour tester tous les services et avoir
    # un rapport complet avant de bloquer.
  fi

done


# -----------------------------------------------------------------------------
# ÉTAPE 3 — Bilan et décision finale
# -----------------------------------------------------------------------------
if [ $TESTED -eq 0 ]; then
  echo "ℹ️   Aucun service avec des tests à lancer."
  exit 0
fi

if [ $FAILED -ne 0 ]; then
  echo "❌  Push bloqué — corrige les tests avant de push."
  exit 1   # Code de sortie non-nul = Git annule le push
fi

echo "✅  Tous les tests passent ($TESTED service(s) testé(s))."
echo ""
exit 0   # Code de sortie 0 = Git autorise le push