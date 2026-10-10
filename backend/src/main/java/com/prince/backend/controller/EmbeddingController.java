package com.prince.backend.controller;

import com.prince.backend.Model.MovieMatch;
import com.prince.backend.service.OllamaEmbeddingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final OllamaEmbeddingService embeddingService;


    public EmbeddingController(
            OllamaEmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    @PostMapping
    public List<MovieMatch> search(
            @RequestBody Map<String, String> request) {

        String text = request.get("text");

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text must not be empty"
            );
        }

        List<MovieMatch> userQueryMatch =
                embeddingService.matching(text);

        return userQueryMatch;
    }
}