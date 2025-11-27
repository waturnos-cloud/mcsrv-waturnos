package com.waturnos.dto.request;

import lombok.Data;

@Data
public class AccessTokenValidateRequest {
    private String email;
    private String phone;
    private String code;
}
