package com.prince.backend.service;

import com.prince.backend.Model.Movie;
import com.prince.backend.Model.MovieEmbedding;
import com.prince.backend.Model.MovieMatch;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class OllamaEmbeddingService {

    private final RestClient restClient;

    @Value("${ollama.model:nomic-embed-text}")
    private String model;

    private final JsonMapper jsonMapper;

    private final List<MovieEmbedding> movieEmbeddingsList =
            new ArrayList<>();

    public OllamaEmbeddingService(
            @Value("${ollama.base-url:http://localhost:11434}")
            String baseUrl,
            JsonMapper jsonMapper) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.jsonMapper = jsonMapper;
    }

    /**
     * Generates an embedding vector for the given text using Ollama.
     */
    public List<Double> generateEmbedding(String userQuery) {

        Map<String, Object> request = Map.of(
                "model", model,
                "input", userQuery
        );

        Map<?, ?> response = restClient.post()
                .uri("/api/embed")
                .body(request)
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("embeddings") == null) {
            throw new IllegalStateException(
                    "Failed to generate embedding from Ollama."
            );
        }

        // Ollama returns a list of embedding vectors.
        List<?> embeddings = (List<?>) response.get("embeddings");

        if (embeddings.isEmpty()) {
            throw new IllegalStateException(
                    "Ollama returned an empty embedding list."
            );
        }

        // Extract the first embedding vector.
        List<?> vector = (List<?>) embeddings.get(0);

        List<Double> result = new ArrayList<>(vector.size());

        for (Object value : vector) {
            result.add(((Number) value).doubleValue());
        }

        return result;
    }

    /**
     * Finds the top 3 movies most similar to the user's query.
     */
    public List<MovieMatch> matching(String userQuery) {

        // Generate the query embedding only once.
        List<Double> queryEmbedding = generateEmbedding(userQuery);

        List<MovieMatch> matches = new ArrayList<>();

        for (MovieEmbedding movieEmbedding : movieEmbeddingsList) {

            double similarity = cosineSimilarity(
                    queryEmbedding,
                    movieEmbedding.getEmbeddings()
            );

            MovieMatch match = new MovieMatch(
                    movieEmbedding.getTitle(),
                    movieEmbedding.getDescription(),
                    similarity
            );

            matches.add(match);
        }

        // Sort by similarity in descending order.
        sortBySimilarity(matches);

        // Return the top 3 matching movies.
        return topKMatches(matches, 3);
    }

    /**
     * Returns the top K matches without exceeding the list size.
     */
    private List<MovieMatch> topKMatches(
            List<MovieMatch> matches,
            int k) {

        int limit = Math.min(Math.max(k, 0), matches.size());

        return new ArrayList<>(matches.subList(0, limit));
    }

    /**
     * Sorts movies from highest similarity to lowest similarity.
     */
    private void sortBySimilarity(List<MovieMatch> matches) {

        Collections.sort(
                matches,
                (first, second) -> Double.compare(
                        second.getSimilarity(),
                        first.getSimilarity()
                )
        );
    }

    /**
     * Calculates cosine similarity between two embedding vectors.
     */
    private double cosineSimilarity(
            List<Double> userQueryEmbedding,
            List<Double> movieEmbedding) {

        if (userQueryEmbedding == null
                || movieEmbedding == null
                || userQueryEmbedding.isEmpty()
                || userQueryEmbedding.size() != movieEmbedding.size()) {

            throw new IllegalArgumentException(
                    "Embedding vectors must be non-empty "
                            + "and have equal dimensions."
            );
        }

        double dotProduct = 0.0;
        double modA = 0.0;
        double modB = 0.0;

        for (int i = 0; i < userQueryEmbedding.size(); i++) {

            double a = userQueryEmbedding.get(i);
            double b = movieEmbedding.get(i);

            dotProduct += a * b;
            modA += a * a;
            modB += b * b;
        }

        double denominator = Math.sqrt(modA) * Math.sqrt(modB);

        if (denominator == 0.0) {
            return 0.0;
        }

        return dotProduct / denominator;
    }

    /**
     * Loads movies from Movies.json and generates their embeddings
     * when the Spring Boot application starts.
     */
    @PostConstruct
    public void initialize() throws IOException {

        ClassPathResource resource =
                new ClassPathResource("Movies.json");

        try (InputStream inputStream = resource.getInputStream()) {

            List<Movie> movies = jsonMapper.readValue(
                    inputStream,
                    new TypeReference<List<Movie>>() {
                    }
            );

            for (Movie movie : movies) {

                List<Double> embeddings =
                        generateEmbedding(movie.getDescription());

                MovieEmbedding movieEmbedding = new MovieEmbedding(
                        movie.getTitle(),
                        embeddings,
                        movie.getDescription()
                );

                movieEmbeddingsList.add(movieEmbedding);
            }
        }

        System.out.println(
                "Successfully generated embeddings for "
                        + movieEmbeddingsList.size()
                        + " movies."
        );
    }
}
