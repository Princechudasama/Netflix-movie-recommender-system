package com.prince.backend.controller;

import com.prince.backend.Model.Movie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MovieController {

    @GetMapping("/movies")
    public List<Movie> movies(){
        return
    }
}
