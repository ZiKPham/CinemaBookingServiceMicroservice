package com.cinema.user_service.domain.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResRegisterDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phone;

}