#!/bin/sh
# =============================================================================
# maven-compile.sh
# Hook Git — exécuté automatiquement au moment du "git push" (stage: push)
#
# Rôle : compiler uniquement les microservices qui contiennent des fichiers
#        Java modifiés dans ce push, plutôt que tout le projet d'un coup.
#
# Pourquoi cibler par service ?
#   Dans un monorepo avec 5+ modules Spring, recompiler l'intégralité à chaque
#   push serait trop lent. On détecte ce qui a réellement changé et on ne
#   touche que ces modules-là.
#
# Mécanique :
#   1. Lister les fichiers Java modifiés depuis le dernier push
#   2. Extraire le nom du dossier racine de chaque fichier = le service
#   3. Pour chaque service : mvn compile -pl <service> -am
#        -pl (project list) → compile ce module spécifiquement
#        -am (also make)    → compile aussi ses dépendances parentes (parent POM)
# =============================================================================


# -----------------------------------------------------------------------------
# ÉTAPE 1 — Détection des services modifiés
#
# On compare HEAD avec le remote tracking branch pour obtenir tous les fichiers
# Java qui ont changé dans l'ensemble des commits qu'on s'apprête à pousser.
# "cut -d'/' -f1" extrait le premier segment du chemin de chaque fichier,
# ce qui correspond au nom du dossier service à la racine du repo.
#
# Exemple :
#   candidate-service/src/main/java/ma/ats/CandidateService.java
#   └─ cut -d'/' -f1 ──────────────────────────────────────────→ "candidate-service"
# -----------------------------------------------------------------------------
CHANGED_SERVICES=$(git diff --cached --name-only \
  | grep "\.java$" \
  | cut -d'/' -f1 \
  | sort -u)   # sort -u : supprime les doublons si plusieurs fichiers du même service changent

# Aucun fichier Java modifié → rien à compiler, on laisse passer
if [ -z "$CHANGED_SERVICES" ]; then
  exit 0
fi

echo ""
echo "🔨  Compilation Maven — services modifiés détectés..."
echo ""

FAILED=0   # Compteur d'échecs — on compile tous les services avant de bloquer


# -----------------------------------------------------------------------------
# ÉTAPE 2 — Compilation service par service
# -----------------------------------------------------------------------------
for SERVICE in $CHANGED_SERVICES; do

  # Certains dossiers à la racine ne sont pas des modules Maven
  # (ex: "scripts/", "docs/", ".github/"). Si pas de pom.xml → on ignore.
  if [ ! -f "$SERVICE/pom.xml" ]; then
    echo "   ⏭️   $SERVICE — pas de pom.xml, ignoré"
    continue
  fi

  echo "   → Compilation de : $SERVICE"

  # -q (quiet) : Maven n'affiche que les erreurs, pas le build complet
  mvn compile -q -pl "$SERVICE" -am 2>&1

  if [ $? -ne 0 ]; then
    echo ""
    echo "   ❌  Échec dans : $SERVICE"
    echo "       Corrige les erreurs de compilation avant de push."
    echo ""
    FAILED=1
    # On ne sort pas tout de suite : on continue la boucle pour
    # compiler les autres services et obtenir un rapport complet en une passe.
  fi

done


# -----------------------------------------------------------------------------
# ÉTAPE 3 — Bilan et décision finale
# -----------------------------------------------------------------------------
if [ $FAILED -ne 0 ]; then
  echo "❌  Push bloqué — au moins un service ne compile pas."
  exit 1   # Code de sortie non-nul = Git annule le push
fi

echo "✅  Compilation OK — tous les services modifiés compilent."
echo ""
exit 0   # Code de sortie 0 = Git autorise le push