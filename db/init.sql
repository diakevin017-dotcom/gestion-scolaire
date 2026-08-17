-- ============================================================
-- Script d'initialisation de la base PostgreSQL
-- Système de Gestion Scolaire — EduPlus (IPD Dakar, L3 2025-2026)
-- Prérequis : créer la base  gestion_scolaire  avant l'exécution.
--   CREATE DATABASE gestion_scolaire;
--   psql -U postgres -d gestion_scolaire -f db/init.sql
--
-- NB : l'application utilise spring.jpa.hibernate.ddl-auto=update et un
-- DataInitializer ; ce script fournit une alternative SQL pure pour le
-- livrable "script SQL d'initialisation avec données de test".
-- ============================================================

-- Suppression dans l'ordre des dépendances (si ré-exécution)
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS inscriptions CASCADE;
DROP TABLE IF EXISTS cours_enseignants CASCADE;
DROP TABLE IF EXISTS user_roles CASCADE;
DROP TABLE IF EXISTS cours CASCADE;
DROP TABLE IF EXISTS enseignants CASCADE;
DROP TABLE IF EXISTS etudiants CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- Rôles
CREATE TABLE roles (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- Utilisateurs
CREATE TABLE users (
    id         BIGSERIAL PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(120) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(50)  NOT NULL,
    last_name  VARCHAR(50)  NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Étudiants
CREATE TABLE etudiants (
    id              BIGSERIAL PRIMARY KEY,
    matricule       VARCHAR(20)  NOT NULL UNIQUE,
    first_name      VARCHAR(50)  NOT NULL,
    last_name       VARCHAR(50)  NOT NULL,
    email           VARCHAR(120) NOT NULL UNIQUE,
    date_naissance  DATE         NOT NULL,
    telephone       VARCHAR(30),
    niveau          VARCHAR(20),
    photo_url       VARCHAR(255),
    document_url    VARCHAR(255),
    user_id         BIGINT REFERENCES users(id)
);

-- Enseignants
CREATE TABLE enseignants (
    id          BIGSERIAL PRIMARY KEY,
    matricule   VARCHAR(20)  NOT NULL UNIQUE,
    first_name  VARCHAR(50)  NOT NULL,
    last_name   VARCHAR(50)  NOT NULL,
    email       VARCHAR(120) NOT NULL UNIQUE,
    specialite  VARCHAR(100),
    photo_url   VARCHAR(255),
    user_id     BIGINT REFERENCES users(id)
);

-- Cours
CREATE TABLE cours (
    id          BIGSERIAL PRIMARY KEY,
    nom         VARCHAR(120) NOT NULL,
    code        VARCHAR(10)  NOT NULL UNIQUE,
    description VARCHAR(500),
    credits     INT          NOT NULL
);

CREATE TABLE cours_enseignants (
    cours_id      BIGINT NOT NULL REFERENCES cours(id) ON DELETE CASCADE,
    enseignant_id BIGINT NOT NULL REFERENCES enseignants(id) ON DELETE CASCADE,
    PRIMARY KEY (cours_id, enseignant_id)
);

-- Inscriptions
CREATE TABLE inscriptions (
    id               BIGSERIAL PRIMARY KEY,
    etudiant_id      BIGINT NOT NULL REFERENCES etudiants(id) ON DELETE CASCADE,
    cours_id         BIGINT NOT NULL REFERENCES cours(id) ON DELETE CASCADE,
    date_inscription DATE   NOT NULL DEFAULT CURRENT_DATE,
    statut           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    note             DOUBLE PRECISION,
    UNIQUE (etudiant_id, cours_id)
);

-- Refresh tokens
CREATE TABLE refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT      NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(64) NOT NULL UNIQUE,
    expiry_date TIMESTAMP   NOT NULL,
    revoked     BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- Données de test
-- ============================================================

INSERT INTO roles (name) VALUES ('ADMIN'), ('ENSEIGNANT'), ('ETUDIANT');

-- Mots de passe (BCrypt) : admin123 / password123
INSERT INTO users (username, email, password, first_name, last_name)
VALUES
  ('admin',        'admin@edupus.sn',        '$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Système', 'Administrateur'),
  ('ens-001',      'awa.diop@edupus.sn',     '$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Awa', 'Diop'),
  ('ens-002',      'moussa.ndiaye@edupus.sn','$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Moussa', 'Ndiaye'),
  ('ens-003',      'fatou.sarr@edupus.sn',   '$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Fatou', 'Sarr'),
  ('etu-2025-001', 'ousmane.ba@edupus.sn',   '$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Ousmane', 'Ba'),
  ('etu-2025-002', 'mariama.faye@edupus.sn', '$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Mariama', 'Faye'),
  ('etu-2025-003', 'ibrahima.gueye@edupus.sn','$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Ibrahima', 'Gueye'),
  ('etu-2025-004', 'aissatou.sy@edupus.sn',  '$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Aissatou', 'Sy'),
  ('etu-2025-005', 'cheikh.diallo@edupus.sn','$2a$10$XwJ8Zc0KvGtD3nqkM8QH9uVq0sVn5kZzWmYkXeFgLrRqQaYbNcOeS', 'Cheikh', 'Diallo');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE (u.username = 'admin' AND r.name = 'ADMIN')
   OR (u.username LIKE 'ens-%' AND r.name = 'ENSEIGNANT')
   OR (u.username LIKE 'etu-%' AND r.name = 'ETUDIANT');

INSERT INTO etudiants (matricule, first_name, last_name, email, date_naissance, telephone, niveau, user_id)
VALUES
  ('ETU-2025-001', 'Ousmane',  'Ba',     'ousmane.ba@edupus.sn',     '2004-03-12', '77-100-00-01', 'L3', (SELECT id FROM users WHERE username='etu-2025-001')),
  ('ETU-2025-002', 'Mariama',  'Faye',   'mariama.faye@edupus.sn',   '2005-07-25', '77-100-00-02', 'L3', (SELECT id FROM users WHERE username='etu-2025-002')),
  ('ETU-2025-003', 'Ibrahima', 'Gueye',  'ibrahima.gueye@edupus.sn', '2003-11-30', '77-100-00-03', 'L2', (SELECT id FROM users WHERE username='etu-2025-003')),
  ('ETU-2025-004', 'Aissatou', 'Sy',     'aissatou.sy@edupus.sn',    '2004-01-18', '77-100-00-04', 'L3', (SELECT id FROM users WHERE username='etu-2025-004')),
  ('ETU-2025-005', 'Cheikh',   'Diallo', 'cheikh.diallo@edupus.sn',  '2005-09-02', '77-100-00-05', 'L1', (SELECT id FROM users WHERE username='etu-2025-005'));

INSERT INTO enseignants (matricule, first_name, last_name, email, specialite, user_id)
VALUES
  ('ENS-001', 'Awa',    'Diop',   'awa.diop@edupus.sn',     'Mathématiques',  (SELECT id FROM users WHERE username='ens-001')),
  ('ENS-002', 'Moussa', 'Ndiaye', 'moussa.ndiaye@edupus.sn','Informatique',   (SELECT id FROM users WHERE username='ens-002')),
  ('ENS-003', 'Fatou',  'Sarr',   'fatou.sarr@edupus.sn',   'Physique-Chimie',(SELECT id FROM users WHERE username='ens-003'));

INSERT INTO cours (nom, code, description, credits)
VALUES
  ('Programmation Java', 'JAVA-101', 'Introduction à Java et POO', 6),
  ('Développement Web',  'WEB-201',  'Spring Boot et APIs REST',    6),
  ('Bases de Données',   'BDD-101',  'SQL et conception relationnelle', 4);

INSERT INTO cours_enseignants (cours_id, enseignant_id)
SELECT c.id, e.id FROM cours c, enseignants e
WHERE (c.code = 'JAVA-101' AND e.matricule = 'ENS-001')
   OR (c.code = 'WEB-201'  AND e.matricule = 'ENS-002')
   OR (c.code = 'BDD-101'  AND e.matricule = 'ENS-002');

INSERT INTO inscriptions (etudiant_id, cours_id, date_inscription)
SELECT st.id, c.id, CURRENT_DATE - 30 FROM etudiants st, cours c
WHERE (st.matricule = 'ETU-2025-001' AND c.code IN ('JAVA-101', 'WEB-201'))
   OR (st.matricule = 'ETU-2025-002' AND c.code = 'WEB-201')
   OR (st.matricule = 'ETU-2025-003' AND c.code = 'BDD-101')
   OR (st.matricule = 'ETU-2025-004' AND c.code = 'JAVA-101')
   OR (st.matricule = 'ETU-2025-005' AND c.code = 'BDD-101');

