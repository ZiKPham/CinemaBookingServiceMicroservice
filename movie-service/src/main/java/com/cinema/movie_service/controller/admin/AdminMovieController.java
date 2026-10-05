package com.cinema.movie_service.controller.admin;

import com.cinema.movie_service.domain.request.MovieSearchCriteria;
import com.cinema.movie_service.domain.request.ReqCreateMovieDTO;
import com.cinema.movie_service.domain.request.ReqUpdateMovieDTO;
import com.cinema.movie_service.domain.response.ResMovieDTO;
import com.cinema.movie_service.domain.response.ResultPaginationDTO;
import com.cinema.movie_service.service.MovieService;
import com.cinema.movie_service.util.error.IdInvalidException;
import com.cinema.movie_service.util.error.NameInvalidException;

import jakarta.validation.Valid;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/movies")
public class AdminMovieController {

    private final MovieService movieService;

    public AdminMovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @PostMapping
    public ResponseEntity<ResMovieDTO> createMovie(@Valid @RequestBody ReqCreateMovieDTO reqCreateMovieDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.movieService.handleCreateMovie(reqCreateMovieDTO));
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

    @PutMapping("/{id}")
    public ResponseEntity<ResMovieDTO> updateMovie(@PathVariable long id, @Valid @RequestBody ReqUpdateMovieDTO reqDTO)
            throws IdInvalidException {
        return ResponseEntity.ok(this.movieService.handleUpdateMovie(id, reqDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable("id") long id) throws IdInvalidException {
        this.movieService.handleDeleteMovie(id);
        return ResponseEntity.ok(null);
    }
}