package com.cinema.cinema_service.domain.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqUpdateCinemaDTO {
    private String name;
    private String address;
    private String description;
}