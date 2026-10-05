package com.cinema.cinema_service.domain.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateRoomDTO {

    private String name;

    @Min(value = 1, message = "Tổng số ghế phải lớn hơn 0")
    private Integer totalSeats;

    private Long cinemaId;
}