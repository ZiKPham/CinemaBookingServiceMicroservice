package com.cinema.cinema_service.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCreateCinemaDTO {

    @NotBlank(message = "Tên rạp chiếu phim không được để trống")
    private String name;

    @NotBlank(message = "Địa chỉ rạp không được để trống")
    private String address;

    private String description;
}