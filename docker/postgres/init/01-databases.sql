-- PostgreSQL : une base par microservice metier
-- Ce script est execute automatiquement au premier demarrage du conteneur postgres
-- via le volume: ./docker/postgres/init:/docker-entrypoint-initdb.d:ro
CREATE DATABASE hirehub_auth OWNER hirehub;
CREATE DATABASE hirehub_offre OWNER hirehub;
CREATE DATABASE hirehub_candidature OWNER hirehub;
CREATE DATABASE hirehub_entretien OWNER hirehub;
