package com.cinema.cinema_service.domain.request;

import com.cinema.cinema_service.util.constant.SeatType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateSeatDTO {

    @NotNull(message = "ID ghế không được để trống")
    private long id;

    @NotBlank(message = "Số ghế (seatNumber) không được để trống")
    private String seatNumber;

    private SeatType seatType;

    @NotNull(message = "Room ID không được để trống")
    private long roomId;
}