package com.prince.backend.controller;

import com.prince.backend.Model.Movie;
import com.prince.backend.Model.MovieMatch;
import com.prince.backend.service.MovieService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;

    }

    // Get all the movies
    @GetMapping("/movies")
    public List<Movie> movies(){
        return   movieService.movies();
    }

    // Recommend 3 movies on the basis of 
    @PostMapping("/search")
    public List<MovieMatch> search(
            @RequestBody Map<String, String> request) {

        String text = request.get("text");

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Text must not be empty"
            );
        }

        List<MovieMatch> userQueryMatch =
                movieService.matching(text);

        return userQueryMatch;
    }
}



