-- Migration Candidature Service
-- Table pour les candidatures

CREATE TABLE IF NOT EXISTS candidatures (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    candidat_id VARCHAR(255) NOT NULL,
    offre_id VARCHAR(255) NOT NULL,
    cv_path VARCHAR(1000),
    lettre_motivation_path VARCHAR(1000),
    status VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    date_soumission TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modification TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour la recherche
CREATE INDEX idx_candidat_id ON candidatures(candidat_id);
CREATE INDEX idx_offre_id ON candidatures(offre_id);
CREATE UNIQUE INDEX idx_candidat_offre ON candidatures(candidat_id, offre_id);

-- Table pour l'historique des changements de statut
CREATE TABLE IF NOT EXISTS historique_statut (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    candidature_id UUID NOT NULL REFERENCES candidatures(id) ON DELETE CASCADE,
    ancien_status VARCHAR(50),
    nouveau_status VARCHAR(50) NOT NULL,
    commentaire VARCHAR(1000),
    utilisateur_id VARCHAR(255),
    date_changement TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour l'historique
CREATE INDEX idx_candidature_id ON historique_statut(candidature_id);
CREATE INDEX idx_date_changement ON historique_statut(date_changement DESC);

