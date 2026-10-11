-- Movies together with their Ollama embedding.
-- vector(768) matches the output size of the nomic-embed-text model.
-- If you switch to an embedding model with a different dimension, add a new
-- migration that changes the column type and re-embeds the movies.
CREATE TABLE movies (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description TEXT         NOT NULL,
    embedding   vector(768)  NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_movies_title UNIQUE (title)
);
