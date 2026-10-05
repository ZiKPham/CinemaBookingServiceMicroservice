package com.cinema.movie_service.controller.client;

import com.cinema.movie_service.domain.request.MovieSearchCriteria;
import com.cinema.movie_service.domain.response.ResMovieDTO;
import com.cinema.movie_service.domain.response.ResultPaginationDTO;
import com.cinema.movie_service.service.MovieService;
import com.cinema.movie_service.util.error.IdInvalidException;
import com.cinema.movie_service.util.error.NameInvalidException;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public ResponseEntity<ResultPaginationDTO> getAllMovies(
            @ParameterObject MovieSearchCriteria criteria, @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(this.movieService.fetchAllMovies(criteria, pageable));
    }

    @GetMapping("/search/{name}")
    public ResponseEntity<List<ResMovieDTO>> getMovieByName(@PathVariable String name) throws NameInvalidException {
        return ResponseEntity.ok(this.movieService.fetchMovieByName(name));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResMovieDTO> getMovieById(@PathVariable("id") long id) throws IdInvalidException {
        ResMovieDTO movie = this.movieService.fetchMovieById(id); // Gọi service lấy phim theo ID
        return ResponseEntity.ok(movie);
    }
}