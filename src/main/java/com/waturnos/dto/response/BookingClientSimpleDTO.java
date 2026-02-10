package com.waturnos.dto.response;

import lombok.Data;

@Data
public class BookingClientSimpleDTO {
    private Long clientId;
    private String clientName;
    private String clientEmail;
    private String clientPhone;
}
