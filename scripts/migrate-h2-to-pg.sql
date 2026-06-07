-- Migration script: H2 → PostgreSQL
-- Run this on an empty PostgreSQL database to recreate the schema.
-- Then use a separate ETL to transfer data from H2.

CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    passwordHash  TEXT         NOT NULL,
    fullName      VARCHAR(100) NOT NULL,
    role          VARCHAR(50)  NOT NULL,
    email         VARCHAR(150) UNIQUE,
    active        BOOLEAN      NOT NULL DEFAULT TRUE,
    lastLogin     TIMESTAMP,
    createdAt     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updatedAt     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS deceased (
    id              BIGSERIAL PRIMARY KEY,
    dossierNumber   VARCHAR(20)  NOT NULL UNIQUE,
    lastName        VARCHAR(100) NOT NULL,
    firstName       VARCHAR(100) NOT NULL,
    birthDate       DATE,
    deathDate       DATE,
    placeOfDeath    TEXT,
    causeOfDeath    TEXT,
    gender          VARCHAR(20),
    nir             VARCHAR(20),
    observations    TEXT,
    createdAt       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updatedAt       TIMESTAMP
);

CREATE TABLE IF NOT EXISTS storage_locations (
    id          BIGSERIAL PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL,
    label       VARCHAR(100) NOT NULL,
    zone        VARCHAR(20)  NOT NULL,
    capacity    INT          NOT NULL DEFAULT 1,
    temperature INT          NOT NULL DEFAULT 4,
    occupied    BOOLEAN      NOT NULL DEFAULT FALSE,
    notes       TEXT,
    createdAt   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS storage_assignments (
    id          BIGSERIAL PRIMARY KEY,
    deceased_id BIGINT       NOT NULL REFERENCES deceased(id),
    location_id BIGINT       NOT NULL REFERENCES storage_locations(id),
    assignedAt  TIMESTAMP    NOT NULL DEFAULT NOW(),
    releasedAt  TIMESTAMP,
    releasedBy  VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS interventions (
    id            BIGSERIAL PRIMARY KEY,
    deceased_id   BIGINT       NOT NULL REFERENCES deceased(id),
    performer_id  BIGINT       NOT NULL REFERENCES users(id),
    type          VARCHAR(50)  NOT NULL,
    scheduledAt   TIMESTAMP    NOT NULL,
    completedAt   TIMESTAMP,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PLANIFIEE',
    report        TEXT,
    productsUsed  TEXT,
    createdAt     TIMESTAMP    NOT NULL DEFAULT NOW(),
    updatedAt     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS exit_authorizations (
    id                BIGSERIAL PRIMARY KEY,
    deceased_id       BIGINT       NOT NULL REFERENCES deceased(id),
    authorized_by_id  BIGINT       NOT NULL REFERENCES users(id),
    authorizedAt      TIMESTAMP    NOT NULL DEFAULT NOW(),
    transportCompany  VARCHAR(150) NOT NULL,
    authorizedPerson  VARCHAR(100),
    notes             TEXT,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    effectiveExitAt   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       REFERENCES users(id),
    action      VARCHAR(50)  NOT NULL,
    entityType  VARCHAR(100),
    entityId    BIGINT,
    details     TEXT,
    timestamp   TIMESTAMP    NOT NULL DEFAULT NOW(),
    ipAddress   VARCHAR(50)
);

-- Indexes
CREATE INDEX idx_deceased_dossier   ON deceased (dossierNumber);
CREATE INDEX idx_deceased_lastName  ON deceased (lastName);
CREATE INDEX idx_deceased_fullName  ON deceased (lastName, firstName);
CREATE INDEX idx_deceased_deathDate ON deceased (deathDate);
CREATE INDEX idx_deceased_createdAt ON deceased (createdAt);

CREATE INDEX idx_storage_assignments_active ON storage_assignments (deceased_id, location_id) WHERE releasedAt IS NULL;

CREATE INDEX idx_interventions_status    ON interventions (status);
CREATE INDEX idx_interventions_scheduled ON interventions (scheduledAt);

CREATE INDEX idx_exit_status   ON exit_authorizations (status);
CREATE INDEX idx_exit_deceased ON exit_authorizations (deceased_id);

CREATE INDEX idx_audit_timestamp ON audit_logs (timestamp);
CREATE INDEX idx_audit_action    ON audit_logs (action);
CREATE INDEX idx_audit_user      ON audit_logs (user_id);

CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_role     ON users (role);
