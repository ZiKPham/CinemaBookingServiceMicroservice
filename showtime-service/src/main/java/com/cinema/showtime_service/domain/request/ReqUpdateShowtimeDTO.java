package com.cinema.showtime_service.domain.request;

import java.time.Instant;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateShowtimeDTO {
    private Instant startTime;

    private Instant endTime;

    @NotNull(message = "Giá vé không được để trống")
    @Min(value = 0, message = "Giá vé phải lớn hơn hoặc bằng 0")
    private Double price;

    @NotNull(message = "movieId không được để trống")
    private Long movieId;

    @NotNull(message = "roomId không được để trống")
    private Long roomId;
}