package com.prince.backend.service;

import com.prince.backend.Model.MovieMatch;
import com.prince.backend.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Semantic movie search: embeds the query with Ollama and lets pgvector
 * find the closest movies.
 */
@Service
public class MovieSearchService {

    private final OllamaEmbeddingService embeddingService;
    private final MovieRepository movieRepository;
    private final int limit;

    public MovieSearchService(
            OllamaEmbeddingService embeddingService,
            MovieRepository movieRepository,
            @Value("${app.search.limit:3}") int limit) {

        this.embeddingService = embeddingService;
        this.movieRepository = movieRepository;
        this.limit = limit;
    }

    /**
     * Finds the movies most similar to the user's query.
     */
    public List<MovieMatch> matching(String userQuery) {

        List<Double> queryEmbedding = embeddingService.generateEmbedding(userQuery);

        return movieRepository.findMostSimilar(queryEmbedding, limit);
    }
}
