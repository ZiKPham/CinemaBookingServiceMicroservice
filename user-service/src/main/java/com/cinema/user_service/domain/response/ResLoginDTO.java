package com.cinema.user_service.domain.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResLoginDTO {
    private String accessToken;

    public ResLoginDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}
