# Netflix-movie-recommender-system
This is the system that recommends movie to the user on the basis of Query semantics with the use of Vector embeddings and vectord DB like pgvector and all the work of REST api works as an Java Spring boot application and React Frontend shows to the user.

## How it works

- Movies are listed in `backend/src/main/resources/Movies.json`.
- On startup the backend embeds each movie description with a local Ollama model (`nomic-embed-text`) and stores the movie and its vector in PostgreSQL (pgvector). Movies that are already stored are skipped, so restarts are fast.
- `POST /api/embeddings` embeds the user's query with Ollama and lets pgvector return the closest movies by cosine similarity.
- The database schema is managed by Flyway migrations in `backend/src/main/resources/db/migration`.

## Run locally

Prerequisites: Java 21, Docker, and [Ollama](https://ollama.com) with the embedding model pulled.

```bash
ollama pull nomic-embed-text

# PostgreSQL with pgvector
docker compose up -d db

# Backend (Flyway creates the schema, then the movies are embedded and stored)
cd backend
./mvnw spring-boot:run
```

Try a search:

```bash
curl -X POST http://localhost:8080/api/embeddings \
  -H "Content-Type: application/json" \
  -d '{"text": "a thief who steals secrets from dreams"}'
```

## Configuration

Defaults work with `docker-compose.yml`; override them with environment variables.

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/movies` |
| `DB_USERNAME` / `DB_PASSWORD` | `movies` / `movies` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` |
| `OLLAMA_MODEL` | `nomic-embed-text` |

## Database and migrations

| Migration | Purpose |
| --- | --- |
| `V1__enable_pgvector.sql` | `CREATE EXTENSION vector` (needs a database user allowed to create it; the Docker image user is) |
| `V2__create_movies_table.sql` | `movies` table with `embedding vector(768)` |
| `V3__create_movies_embedding_index.sql` | HNSW index for cosine distance |

The embedding column size (768) is tied to `nomic-embed-text`. To use a model with a different dimension, add a new migration that changes the column, update `ollama.embedding-dimensions`, and re-embed the movies. Never edit a migration that has already been applied; add a new one instead.

To add movies, append them to `Movies.json` and restart; only the new titles are embedded.

## Tests

`mvn test` needs the database running (`docker compose up -d db`). Ollama is not needed for tests.
