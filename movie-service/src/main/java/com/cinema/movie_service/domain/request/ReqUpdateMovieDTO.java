package com.cinema.movie_service.domain.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateMovieDTO {

    private String name;

    private String description;

    private String director;

    private String cast;

    private String genre;

    @Min(value = 1, message = "Thời lượng phim phải lớn hơn 0")
    private Integer duration;

    private LocalDate releaseDate;

    private String poster;

    private String trailer;

    private String language;

    private String ageRestriction;

    private Boolean active;
}