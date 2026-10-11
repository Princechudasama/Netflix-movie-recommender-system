package com.prince.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generates embeddings with a local Ollama model.
 */
@Service
public class OllamaEmbeddingService {

    private final RestClient restClient;

    @Value("${ollama.model:nomic-embed-text}")
    private String model;

    // Must match the vector(N) column created by the Flyway migrations.
    @Value("${ollama.embedding-dimensions:768}")
    private int expectedDimensions;

    public OllamaEmbeddingService(
            @Value("${ollama.base-url:http://localhost:11434}")
            String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
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

        if (vector.size() != expectedDimensions) {
            throw new IllegalStateException(
                    "Ollama model '" + model + "' returned "
                            + vector.size() + " dimensions, but the database column is vector("
                            + expectedDimensions + "). Use a matching model or add a Flyway "
                            + "migration that changes the column size."
            );
        }

        List<Double> result = new ArrayList<>(vector.size());

        for (Object value : vector) {
            result.add(((Number) value).doubleValue());
        }

        return result;
    }
}
