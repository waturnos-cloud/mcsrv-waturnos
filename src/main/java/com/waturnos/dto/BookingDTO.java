package com.waturnos.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Long id;
    private Long tenantId;
    private Long customerId;
    private Long serviceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
}