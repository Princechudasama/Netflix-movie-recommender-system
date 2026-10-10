package com.prince.backend.controller;

import com.prince.backend.Model.MovieMatch;
import com.prince.backend.service.MovieSearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final MovieSearchService movieSearchService;


    public EmbeddingController(
            MovieSearchService movieSearchService) {
        this.movieSearchService = movieSearchService;
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
                movieSearchService.matching(text);

        return userQueryMatch;
    }
}
