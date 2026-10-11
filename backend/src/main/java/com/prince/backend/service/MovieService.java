package com.prince.backend.service;

import com.prince.backend.Model.Movie;
import com.prince.backend.repository.MovieRepository;
import com.prince.backend.repository.MoviesRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    private final MoviesRepository moviesRepository;

    public MovieService(MoviesRepository moviesRepository) {
        this.moviesRepository = moviesRepository;
    }


    public List<Movie> movies(){
        return  moviesRepository.
    }
}
