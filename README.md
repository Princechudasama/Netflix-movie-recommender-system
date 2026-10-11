# Netflix-movie-recommender-system
This is the system that recommends movie to the user on the basis of Query semantics with the use of Vector embeddings and vectord DB like pgvector and all the work of REST api works as an Java Spring boot application and React Frontend shows to the user.

## How it works

- Movies are listed in `backend/src/main/resources/Movies.json`.
- On startup the backend embeds each movie description with a local Ollama model (`nomic-embed-text`) and stores the movie and its vector in PostgreSQL (pgvector). Movies that are already stored are skipped, so restarts are fast.
- `POST /api/embeddings` embeds the user's query with Ollama and lets pgvector return the closest movies by cosine similarity.
- The database schema is managed by Flyway migrations in `backend/src/main/resources/db/migration`.

## Prerequisites

- Java 21
- PostgreSQL 13 or newer **with the pgvector extension installed on the server** (pgAdmin is only a client and does not provide it)
- [Ollama](https://ollama.com) with the embedding model: `ollama pull nomic-embed-text`

## 1. Set up PostgreSQL with pgvector

Docker is optional. Use your own PostgreSQL (option A) or the Docker image (option B).

### Option A: your own PostgreSQL (managed with pgAdmin)

1. **Install pgvector into the PostgreSQL server** (once).

   Windows: install "Desktop development with C++" from the Visual Studio Build Tools, open the **x64 Native Tools Command Prompt for VS** *as administrator*, and run (set `PGROOT` to your PostgreSQL folder and version):

   ```bat
   set "PGROOT=C:\Program Files\PostgreSQL\17"
   cd %TEMP%
   git clone --branch v0.8.7 https://github.com/pgvector/pgvector.git
   cd pgvector
   nmake /F Makefile.win
   nmake /F Makefile.win install
   ```

   Then restart the PostgreSQL service (`services.msc`, e.g. "postgresql-x64-17").

   Linux / macOS: install the package for your PostgreSQL version, e.g. `sudo apt install postgresql-17-pgvector` or `brew install pgvector`, and restart PostgreSQL.

2. **Create the database and enable the extension** in pgAdmin (Query Tool, connected as a superuser such as `postgres`):

   ```sql
   CREATE DATABASE movies;
   ```

   Then open a Query Tool on the new `movies` database and run:

   ```sql
   CREATE EXTENSION IF NOT EXISTS vector;
   SELECT extversion FROM pg_extension WHERE extname = 'vector';
   ```

   Running this yourself means the application's database user does not need superuser rights; the Flyway migration `V1` then simply finds the extension already present.

3. **Tell the backend how to connect.** Defaults are `jdbc:postgresql://localhost:5432/movies` with user/password `movies`/`movies`. Override them with environment variables (or edit `application.properties`).

   PowerShell:

   ```powershell
   $env:DB_USERNAME = "postgres"
   $env:DB_PASSWORD = "your-password"
   ```

   If your IDE has a run configuration, set the same variables there.

### Option B: Docker

```bash
docker compose up -d db
```

This starts PostgreSQL with pgvector on port 5432 with the default credentials above. If a local PostgreSQL already uses 5432, change the mapping in `docker-compose.yml` (for example `"5433:5432"`) and set `DB_URL=jdbc:postgresql://localhost:5433/movies`.

## 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

Flyway creates the schema, then the movies are embedded and stored.

Try a search:

```bash
curl -X POST http://localhost:8080/api/embeddings \
  -H "Content-Type: application/json" \
  -d '{"text": "a thief who steals secrets from dreams"}'
```

## Configuration

| Variable | Default |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/movies` |
| `DB_USERNAME` / `DB_PASSWORD` | `movies` / `movies` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` |
| `OLLAMA_MODEL` | `nomic-embed-text` |

## Database and migrations

| Migration | Purpose |
| --- | --- |
| `V1__enable_pgvector.sql` | `CREATE EXTENSION IF NOT EXISTS vector` (fails with `extension "vector" is not available` if pgvector is not installed on the server) |
| `V2__create_movies_table.sql` | `movies` table with `embedding vector(768)` |
| `V3__create_movies_embedding_index.sql` | HNSW index for cosine distance |

The embedding column size (768) is tied to `nomic-embed-text`. To use a model with a different dimension, add a new migration that changes the column, update `ollama.embedding-dimensions`, and re-embed the movies. Never edit a migration that has already been applied; add a new one instead.

To add movies, append them to `Movies.json` and restart; only the new titles are embedded.

## Tests

`mvn test` needs the database reachable with the same settings as above. Ollama is not needed for tests.
