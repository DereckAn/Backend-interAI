-- Initial schema creation
-- Activar extensión para UUIDs (si usas PostgreSQL)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Definir tipo para roles de usuario
CREATE TYPE AuthRole AS ENUM ('USER', 'ADMIN');

-- Tabla users (información básica de usuarios)
CREATE TABLE users
(
    id              UUID     NOT NULL DEFAULT uuid_generate_v4(),
    name            VARCHAR(255),
    email           VARCHAR(255) UNIQUE,
    "emailVerified" TIMESTAMPTZ,
    image           TEXT,
    role            AuthRole NOT NULL DEFAULT 'USER',
    password        TEXT,
    created_at      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Tabla accounts (para cuentas OAuth)
CREATE TABLE accounts
(
    id                  UUID         NOT NULL DEFAULT uuid_generate_v4(),
    "userId"            UUID         NOT NULL,
    type                VARCHAR(255) NOT NULL,
    provider            VARCHAR(255) NOT NULL,
    "providerAccountId" VARCHAR(255) NOT NULL,
    refresh_token       TEXT,
    access_token        TEXT,
    expires_at          BIGINT,
    id_token            TEXT,
    scope               TEXT,
    session_state       TEXT,
    token_type          TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_user FOREIGN KEY ("userId") REFERENCES users (id) ON DELETE CASCADE
);

-- Tabla sessions (para manejar sesiones de usuario)
CREATE TABLE sessions
(
    id             UUID        NOT NULL DEFAULT uuid_generate_v4(),
    "sessionToken" TEXT        NOT NULL UNIQUE,
    "userId"       UUID        NOT NULL,
    expires        TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_user FOREIGN KEY ("userId") REFERENCES users (id) ON DELETE CASCADE
);

-- Tabla verification_tokens (para verificación de correo)
CREATE TABLE verification_tokens
(
    identifier TEXT        NOT NULL,
    expires    TIMESTAMPTZ NOT NULL,
    token      TEXT        NOT NULL,
    PRIMARY KEY (identifier, token)
);

-- Tabla topics (temas de entrevistas)
CREATE TABLE topics
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Tabla languages (lenguajes de programación)
CREATE TABLE languages
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Tabla difficulties (niveles de dificultad)
CREATE TABLE difficulties
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    level       VARCHAR(50) UNIQUE NOT NULL,
    description TEXT,
    years_of_experience int8
);

-- Tabla questions (preguntas de entrevistas)
CREATE TABLE questions
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    topic_id      UUID REFERENCES topics (id) ON DELETE CASCADE,
    language_id   UUID REFERENCES languages (id) ON DELETE CASCADE,
    difficulty_id UUID REFERENCES difficulties (id) ON DELETE CASCADE,
    question_text TEXT NOT NULL,
    sample_answer TEXT,
    created_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Tabla interviews (registro de entrevistas)
CREATE TABLE interviews
(
    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id          UUID REFERENCES users (id) ON DELETE CASCADE,
    topic_id         UUID REFERENCES topics (id) ON DELETE SET NULL,
    language_id      UUID REFERENCES languages (id) ON DELETE SET NULL,
    difficulty_id    UUID REFERENCES difficulties (id) ON DELETE SET NULL,
    job_description  TEXT,
    experience_years NUMERIC,
    start_time       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    end_time         TIMESTAMP,
    duration         INTERVAL GENERATED ALWAYS AS (end_time - start_time) STORED,
    video_url        VARCHAR(255),
    audio_url        VARCHAR(255),
    status           VARCHAR(50)      DEFAULT 'in_progress'
);

-- Tabla interview_questions (asocia preguntas a entrevistas)
CREATE TABLE interview_questions
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    interview_id UUID REFERENCES interviews (id) ON DELETE CASCADE,
    question_id  UUID REFERENCES questions (id) ON DELETE CASCADE,
    user_answer  TEXT,
    asked_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Tabla feedback (retroalimentación de entrevistas)
CREATE TABLE feedback
(
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    interview_id        UUID UNIQUE REFERENCES interviews (id) ON DELETE CASCADE,
    technical_score     INT CHECK (technical_score >= 0 AND technical_score <= 100),
    non_technical_score INT CHECK (non_technical_score >= 0 AND
                                   non_technical_score <= 100),
    posture_notes       TEXT,
    voice_tone_notes    TEXT,
    clothing_notes      TEXT,
    general_comments    TEXT,
    improvement_tips    JSONB,
    would_hire          BOOLEAN,
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

-- Tabla files (archivos de usuarios: currículums y descripciones de trabajo)
CREATE TABLE files
(
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    original_filename VARCHAR(255) NOT NULL,
    stored_filename   VARCHAR(255) NOT NULL UNIQUE,
    content_type      VARCHAR(100) NOT NULL,
    file_size         BIGINT       NOT NULL,
    bucket_name       VARCHAR(100) NOT NULL,
    file_type         VARCHAR(50)  NOT NULL CHECK (file_type IN ('RESUME', 'JOB_DESCRIPTION')),
    user_id           UUID         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    upload_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla resumes (currículums de usuarios) - DEPRECATED, use files table instead
CREATE TABLE resumes
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID REFERENCES users (id) ON DELETE CASCADE,
    file_path   VARCHAR(255),
    uploaded_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);