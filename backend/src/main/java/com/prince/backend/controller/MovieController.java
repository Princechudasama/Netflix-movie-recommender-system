package com.prince.backend.controller;

import com.prince.backend.Model.Movie;
import com.prince.backend.service.MovieService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
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
}
