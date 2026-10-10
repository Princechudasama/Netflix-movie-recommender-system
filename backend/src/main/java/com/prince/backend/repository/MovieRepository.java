package com.prince.backend.repository;

import com.prince.backend.Model.Movie;
import com.prince.backend.Model.MovieMatch;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Stores movies and their embeddings in PostgreSQL (pgvector).
 */
@Repository
public class MovieRepository {

    private final JdbcTemplate jdbcTemplate;

    public MovieRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns the titles of all movies already stored.
     */
    public Set<String> findAllTitles() {
        return new HashSet<>(
                jdbcTemplate.queryForList("SELECT title FROM movies", String.class)
        );
    }

    /**
     * Inserts a movie with its embedding. A movie whose title already exists is left untouched.
     */
    public void save(Movie movie, List<Double> embedding) {

        jdbcTemplate.update(
                """
                INSERT INTO movies (title, description, embedding)
                VALUES (?, ?, ?::vector)
                ON CONFLICT (title) DO NOTHING
                """,
                movie.getTitle(),
                movie.getDescription(),
                PgVector.toLiteral(embedding)
        );
    }

    /**
     * Returns the movies closest to the given embedding, most similar first.
     * Similarity is cosine similarity (1 - cosine distance), so values match
     * the previous in-memory implementation.
     */
    public List<MovieMatch> findMostSimilar(List<Double> queryEmbedding, int limit) {

        String vector = PgVector.toLiteral(queryEmbedding);

        return jdbcTemplate.query(
                """
                SELECT title, description, 1 - (embedding <=> ?::vector) AS similarity
                FROM movies
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """,
                (resultSet, rowNumber) -> {
                    MovieMatch match = new MovieMatch();
                    match.setTitle(resultSet.getString("title"));
                    match.setDescription(resultSet.getString("description"));
                    match.setSimilarity(resultSet.getDouble("similarity"));
                    return match;
                },
                vector,
                vector,
                limit
        );
    }
}
