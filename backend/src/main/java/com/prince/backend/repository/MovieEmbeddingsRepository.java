package com.prince.backend.repository;

import com.prince.backend.Model.Movie;
import com.prince.backend.Model.MovieEmbed;
import com.prince.backend.Model.MovieMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface MovieEmbeddingsRepository extends JpaRepository<MovieEmbed, Long> {

    /**
     * Returns all existing movie titles.
     */
    @Query(value = "SELECT title FROM movie_embeddings", nativeQuery = true)
    List<String> findAllTitles();

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO movie_embeddings (title, description, embedding)
        VALUES (:title, :description, CAST(:embedding AS vector))
        ON CONFLICT (title) DO NOTHING
        """, nativeQuery = true)
    int saveMovie(
            @Param("title") String title,
            @Param("description") String description,
            @Param("embedding") String embedding
    );

    /**
     * Finds the most similar movies using cosine similarity.
     */
    @Query(value = """
        SELECT title,
               description,
               1 - (embedding <=> CAST(:embedding AS vector)) AS similarity
        FROM movie_embeddings
        ORDER BY embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findMostSimilarRaw(
            @Param("embedding") String embedding,
            @Param("limit") int limit
    );

    /**
     * Converts the embedding and maps query results to MovieMatch objects.
     */
    default List<MovieMatch> findMostSimilarRaw(
            List<Double> queryEmbedding,
            int limit
    ) {
        String vector = PgVector.toLiteral(queryEmbedding);

        return findMostSimilarRaw(vector, limit)
                .stream()
                .map(row -> {
                    MovieMatch match = new MovieMatch();
                    match.setTitle((String) row[0]);
                    match.setDescription((String) row[1]);
                    match.setSimilarity(((Number) row[2]).doubleValue());
                    return match;
                })
                .toList();
    }

    /**
     * Inserts a movie embedding if its title doesn't already exist.
     */

}