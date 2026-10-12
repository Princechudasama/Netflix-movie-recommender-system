package com.prince.backend.service;

import com.prince.backend.Model.Movie;
import com.prince.backend.Model.MovieMatch;
import com.prince.backend.repository.MovieEmbeddingsRepository;
import com.prince.backend.repository.MoviesDetailsRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MoviesDetailsRepository moviesDetailsRepository;

    private final OllamaEmbeddingService embeddingService;
    private final MovieEmbeddingsRepository movieEmbeddingsRepository;
    private final int limit;

    public MovieService(MoviesDetailsRepository moviesDetailsRepository, OllamaEmbeddingService embeddingService,
                        MovieEmbeddingsRepository movieEmbeddingsRepository,
                        @Value("${app.search.limit:3}") int limit) {
        this.moviesDetailsRepository = moviesDetailsRepository;
        this.embeddingService = embeddingService;
        this.movieEmbeddingsRepository = movieEmbeddingsRepository;
        this.limit = limit;
    }


    public List<Movie> movies(){
        return  moviesDetailsRepository.findAll();
    }


    /**
     * Finds the movies most similar to the user's query.
     */
    public List<MovieMatch> matching(String userQuery) {

        List<Double> queryEmbedding = embeddingService.generateEmbedding(userQuery);

        return movieEmbeddingsRepository.findMostSimilarRaw(queryEmbedding, limit);
    }
}
