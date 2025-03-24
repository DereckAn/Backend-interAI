-- Activar extensión para UUIDs
CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";

-- Definir tipo para roles
CREATE TYPE AuthRole AS ENUM ('USER', 'ADMIN');

-- Tabla users (combinada)
CREATE TABLE users
(
    id              UUID     NOT NULL DEFAULT uuid_generate_v4(),
    name            VARCHAR(255),
    email           VARCHAR(255) UNIQUE,
    "emailVerified" TIMESTAMPTZ,
    image           TEXT,
    role            AuthRole NOT NULL DEFAULT 'USER',
    username        VARCHAR(100) UNIQUE,
    created_at      TIMESTAMP         DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

-- Tabla accounts (para OAuth)
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

-- Tabla verification_token (para verificación de correo)
CREATE TABLE verification_token
(
    identifier TEXT        NOT NULL,
    expires    TIMESTAMPTZ NOT NULL,
    token      TEXT        NOT NULL,
    PRIMARY KEY (identifier, token)
);

-- Resto de las tablas de tu proyecto
CREATE TABLE topics
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    created_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE languages
(
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE difficulties
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    level       VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

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

CREATE TABLE interviews
(
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID REFERENCES users (id) ON DELETE CASCADE,
    topic_id      UUID REFERENCES topics (id) ON DELETE SET NULL,
    language_id   UUID REFERENCES languages (id) ON DELETE SET NULL,
    difficulty_id UUID REFERENCES difficulties (id) ON DELETE SET NULL,
    start_time    TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    end_time      TIMESTAMP,
    duration INTERVAL GENERATED ALWAYS AS (end_time - start_time) STORED,
    video_url     VARCHAR(255),
    audio_url     VARCHAR(255),
    status        VARCHAR(50)      DEFAULT 'in_progress'
);

CREATE TABLE interview_questions
(
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    interview_id UUID REFERENCES interviews (id) ON DELETE CASCADE,
    question_id  UUID REFERENCES questions (id) ON DELETE CASCADE,
    user_answer  TEXT,
    asked_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE feedback
(
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    interview_id        UUID UNIQUE REFERENCES interviews (id) ON DELETE CASCADE,
    technical_score     INT CHECK (technical_score >= 0 AND technical_score <= 100),
    non_technical_score INT CHECK (non_technical_score >= 0 AND non_technical_score <= 100),
    posture_notes       TEXT,
    voice_tone_notes    TEXT,
    clothing_notes      TEXT,
    general_comments    TEXT,
    improvement_tips    JSONB,
    would_hire          BOOLEAN,
    created_at          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO topics (name, description)
VALUES ('Fullstack', 'Preguntas sobre desarrollo fullstack'),
       ('Backend', 'Preguntas sobre desarrollo backend'),
       ('Frontend', 'Preguntas sobre desarrollo frontend'),
       ('DevOps', 'Preguntas sobre DevOps'),
       ('Data Science', 'Preguntas sobre ciencia de datos'),
       ('Mobile', 'Preguntas sobre desarrollo móvil'),
       ('Machine Learning', 'Preguntas sobre aprendizaje automático'),
       ('Algorithms', 'Preguntas sobre algoritmos'),
       ('System Design', 'Preguntas sobre diseño de sistemas'),
       ('Testing', 'Preguntas sobre pruebas de software'),
       ('Cyber Security', 'Preguntas sobre seguridad informática'),
       ('Cloud Computing', 'Preguntas sobre computación en la nube'),
       ('Blockchain', 'Preguntas sobre blockchain'),
       ('IoT', 'Preguntas sobre Internet de las cosas'),
       ('AR/VR', 'Preguntas sobre realidad aumentada y virtual'),
       ('Quantum Computing', 'Preguntas sobre computación cuántica'),
       ('Game Development', 'Preguntas sobre desarrollo de videojuegos');

INSERT INTO languages (name)
VALUES ('Java'),
       ('Python'),
       ('JavaScript'),
       ('Ruby'),
       ('C#'),
       ('PHP'),
       ('Go'),
       ('Rust'),
       ('Swift'),
       ('Kotlin'),
       ('TypeScript'),
       ('Scala');

INSERT INTO difficulties (level, description)
VALUES ('Fácil', 'Preguntas básicas para principiantes'),
       ('Medio', 'Preguntas intermedias'),
       ('Difícil', 'Preguntas avanzadas para expertos');

INSERT INTO questions (topic_id, language_id, difficulty_id, question_text, sample_answer)
VALUES ((SELECT id FROM topics WHERE name = 'Fullstack'),
        (SELECT id FROM languages WHERE name = 'Java'),
        (SELECT id FROM difficulties WHERE level = 'Fácil'),
        '¿Qué es una interfaz en Java?',
        'Una interfaz en Java es una colección de métodos abstractos que una clase puede implementar.');