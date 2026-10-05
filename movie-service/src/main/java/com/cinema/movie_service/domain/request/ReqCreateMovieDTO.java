package com.cinema.movie_service.domain.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateMovieDTO {

    @NotBlank(message = "Tên phim không được để trống")
    private String name;

    private String description;

    private String director;

    private String cast;

    private String genre;

    @NotNull(message = "Thời lượng phim không được để trống")
    @Min(value = 1, message = "Thời lượng phim phải lớn hơn 0")
    private Integer duration;

    @NotNull(message = "Ngày khởi chiếu không được để trống")
    private LocalDate releaseDate;

    private String poster;

    private String trailer;

    private String language;

    private String ageRestriction;

    private boolean active = true;
}