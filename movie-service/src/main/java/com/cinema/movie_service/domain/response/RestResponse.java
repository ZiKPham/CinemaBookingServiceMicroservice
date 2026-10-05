package com.cinema.movie_service.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponse<T> {
    private int statusCode;
    private String message;
    private T data;
    private Object error;
}
