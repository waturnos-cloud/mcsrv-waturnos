package com.waturnos.dto.request;

import lombok.Data;

@Data
public class AccessTokenRequest {
    private String email;
    private String phone;
}
