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
--     username        VARCHAR(100) UNIQUE,
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

INSERT INTO topics (name, description)
VALUES ('Fullstack',
        'Preguntas sobre desarrollo de aplicaciones que involucran tanto el frontend como el backend, utilizando tecnologías como React, Angular, Vue.js, Node.js, Django, y más.'),
       ('Backend',
        'Preguntas sobre desarrollo de la parte del servidor, incluyendo bases de datos, APIs, frameworks como Spring Boot, Express.js, y lenguajes como Java, Python, y PHP.'),
       ('Frontend',
        'Preguntas sobre desarrollo de interfaces de usuario, utilizando tecnologías como HTML, CSS, JavaScript, React, Angular, y Vue.js.'),
       ('DevOps',
        'Preguntas sobre la integración y despliegue continuo, gestión de infraestructura, herramientas como Docker, Kubernetes, Jenkins, y prácticas de DevOps.'),
       ('Data Science',
        'Preguntas sobre análisis de datos, estadísticas, visualización de datos, uso de herramientas como Python, R, y plataformas de análisis de datos.'),
       ('Mobile',
        'Preguntas sobre desarrollo de aplicaciones móviles, utilizando tecnologías como Android, iOS, Flutter, y React Native.'),
       ('Machine Learning',
        'Preguntas sobre algoritmos de aprendizaje automático, modelado de datos, uso de frameworks como TensorFlow, PyTorch, y Scikit-learn.'),
       ('Algorithms',
        'Preguntas sobre diseño y análisis de algoritmos, estructuras de datos, y problemas de programación competitiva.'),
       ('System Design',
        'Preguntas sobre diseño de sistemas escalables, arquitectura de software, bases de datos distribuidas, y diseño de microservicios.'),
       ('Testing',
        'Preguntas sobre pruebas de software, tipos de pruebas (unitarias, de integración, de aceptación), herramientas de testing, y estrategias de testing.'),
       ('Cyber Security',
        'Preguntas sobre seguridad informática, protección de datos, prevención de ataques, uso de herramientas de seguridad, y políticas de seguridad.'),
       ('Cloud Computing',
        'Preguntas sobre computación en la nube, servicios de AWS, Azure, Google Cloud, y prácticas de despliegue en la nube.'),
       ('Blockchain',
        'Preguntas sobre tecnología blockchain, criptomonedas, contratos inteligentes, y aplicaciones descentralizadas.'),
       ('IoT',
        'Preguntas sobre Internet de las cosas, dispositivos conectados, comunicación entre dispositivos, y plataformas de IoT.'),
       ('AR/VR',
        'Preguntas sobre realidad aumentada y virtual, desarrollo de aplicaciones AR/VR, y uso de tecnologías como Unity y Unreal Engine.'),
       ('Quantum Computing',
        'Preguntas sobre computación cuántica, algoritmos cuánticos, y aplicaciones de la computación cuántica.'),
       ('Game Development',
        'Preguntas sobre desarrollo de videojuegos, motores de juegos como Unity y Unreal Engine, diseño de juegos, y programación de juegos.');

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
VALUES ('Junior',
        'Preguntas básicas y fundamentales para desarrolladores principiantes. Ideal para aquellos que están comenzando en el campo y necesitan construir una base sólida.'),
       ('MidLevel',
        'Preguntas intermedias que requieren un conocimiento más profundo de los conceptos y herramientas. Ideal para desarrolladores con experiencia en proyectos prácticos.'),
       ('Senior',
        'Preguntas avanzadas y complejas para expertos en el campo. Ideal para aquellos que tienen años de experiencia y buscan desafíos técnicos más difíciles.');
