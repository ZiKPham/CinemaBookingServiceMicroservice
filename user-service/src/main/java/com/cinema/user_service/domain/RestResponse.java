package com.cinema.user_service.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResponse<T> {
    private int statusCode;
    private Object error;
    private Object message;
    private T data;
}
