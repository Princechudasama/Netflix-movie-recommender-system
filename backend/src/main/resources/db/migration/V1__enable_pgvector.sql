-- Enables the pgvector extension (vector type + distance operators).
-- Requires a PostgreSQL build that ships pgvector, e.g. the pgvector/pgvector Docker image.
CREATE EXTENSION IF NOT EXISTS vector;
