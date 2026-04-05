# Répartition des tâches — HireHub (équipe de 4)

Document court pour le suivi ; la **description détaillée** et le découpage parallèle sont dans **`docs/PROJET_HIREHUB_COMPLET.md`**. Contrats REST : **`docs/API-CONTRACTS.md`**.

**Stack :** Spring Boot 3.2, Spring Cloud, **PostgreSQL 17**, Thymeleaf.

---

## Répartition synthétique (4 personnes, modules Maven)

| Personne | Module(s) principal(aux) | Focus |
|----------|---------------------------|--------|
| **A** | `hirehub-parent`, `hirehub-common`, `eureka-server`, `config-server`, `notification-service`, Docker | DTO, infra, emails (dont approbation / refus recruteur), BDD init |
| **B** | `api-gateway`, `frontend-service` | JWT Gateway, Thymeleaf (2 inscriptions, lock recruteur), Feign |
| **C** | `auth-service` | Register candidat/recruteur, JWT + `recruteurApprouve`, endpoints admin validation |
| **D** | `offre-service`, `candidature-service`, `entretien-service` | Métier offres, candidatures, entretiens |

Chacun peut avancer sur **son module** dès que les **DTO et URLs** du contrat sont figés (session commune semaine 1).

---

## Règles pour limiter les blocages

1. Ne pas attendre l’implémentation complète : **stubs** ou réponses statiques côté frontend / mocks Feign.
2. Branches : `feat/<module>` ou `feat/<nom>-<tâche>`, PR avec relecture.
3. PostgreSQL : scripts d’init (ou Flyway) **dans le module** concerné, documentés.

---

## Tâches partagées (toute l’équipe)

| Tâche | Détail |
|-------|--------|
| Schéma & démo | Architecture, ordre de démarrage, scénario bout en bout (inscription recruteur → lock UI → admin approuve → action offre) |
| Revue de code | Au moins une relecture avant merge sur `main` |
| Documentation | README, `docs/`, captures parcours |

---

## Fichiers de référence

- `docs/Repartition_par_microservices.md` — **répartition par microservice** (tableau + graphe de dépendances)
- `docs/Repartition_microservices.csv` — même vue export tableur
- `docs/PROJET_HIREHUB_COMPLET.md` — vision produit, stack, **Personne A/B/C/D** détaillée
- `docs/API-CONTRACTS.md` — endpoints inscription + validation recruteur
- `docs/PARCOURS_RECRUTEUR.md` — inscription choix + lock jusqu’à approbation
- `docs/ROUTES_UI.md` — routes Thymeleaf
- `docs/Repartition_taches_HireHub.csv` — vue tabulaire par phase

*Mise à jour : équipe 4, inscription candidat/recruteur, validation admin, UI verrouillée.*
