package com.cinema.movie_service.domain.request;

import java.time.LocalDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqMovieDTO {
    @NotBlank(message = "Tên phim không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Thời lượng phim không được để trống")
    @Min(value = 1, message = "Thời lượng phim phải lớn hơn 0")
    private Integer duration; // tính bằng phút

    private LocalDate releaseDate;

    private String director;

    private String actors;

    private String category;

    private String posterUrl;
}