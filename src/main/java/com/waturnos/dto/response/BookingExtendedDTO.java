package com.waturnos.dto.response;

import lombok.Data;

@Data
public class BookingExtendedDTO {

    private Long id;

    private Long serviceId;
    private String serviceName;

    private Long providerId;
    private String providerName;

    private Long clientId;
    private String clientName;
    private String clientPhone;
    private String clientEmail;

    private String status; // FREE, RESERVED, CANCELLED, COMPLETED, NO-SHOW
    
    private String freeSlots;

    private String startTime; // ISO 8601 string
    private String endTime;

    private Integer durationMinutes;

    // opcional si usás agenda por sede
    private Long locationId;
    private String locationName;
    private String locationAddress;
}