package com.cinema.movie_service.domain.response;

import java.time.Instant;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResMovieDTO {
    private long id;
    private String name;
    private String description;
    private String director;
    private String cast;
    private String genre;
    private Integer duration;
    private LocalDate releaseDate;
    private String poster;
    private String trailer;
    private String language;
    private String ageRestriction;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}