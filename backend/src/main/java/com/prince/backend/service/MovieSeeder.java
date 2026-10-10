package com.prince.backend.service;

import com.prince.backend.Model.Movie;
import com.prince.backend.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Loads Movies.json into PostgreSQL when the application starts.
 *
 * Only movies that are not in the database yet (matched by title) are
 * embedded and inserted, so restarts do not call Ollama again. Runs after
 * Flyway has migrated the schema.
 */
@Component
@ConditionalOnProperty(
        name = "app.seed.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class MovieSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MovieSeeder.class);

    private final MovieRepository movieRepository;
    private final OllamaEmbeddingService embeddingService;
    private final JsonMapper jsonMapper;

    public MovieSeeder(
            MovieRepository movieRepository,
            OllamaEmbeddingService embeddingService,
            JsonMapper jsonMapper) {

        this.movieRepository = movieRepository;
        this.embeddingService = embeddingService;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        List<Movie> movies;

        try (InputStream inputStream =
                     new ClassPathResource("Movies.json").getInputStream()) {

            movies = jsonMapper.readValue(
                    inputStream,
                    new TypeReference<List<Movie>>() {
                    }
            );
        }

        Set<String> existingTitles = movieRepository.findAllTitles();

        int inserted = 0;

        for (Movie movie : movies) {

            if (existingTitles.contains(movie.getTitle())) {
                continue;
            }

            List<Double> embedding =
                    embeddingService.generateEmbedding(movie.getDescription());

            movieRepository.save(movie, embedding);
            existingTitles.add(movie.getTitle());
            inserted++;
        }

        log.info(
                "Movie seeding finished: {} new movies embedded and stored, {} already present.",
                inserted,
                movies.size() - inserted
        );
    }
}
