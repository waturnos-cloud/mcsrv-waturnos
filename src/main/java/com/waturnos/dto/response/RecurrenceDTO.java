package com.waturnos.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

import com.waturnos.enums.RecurrenceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurrenceDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long serviceId;
    private String serviceName;
    private Long providerId;
    private String providerName;
    private Integer dayOfWeek; // 1=Lunes, 7=Domingo
    private LocalTime timeSlot;
    private RecurrenceType recurrenceType;
    private Integer occurrenceCount;
    private LocalDate endDate;
    private Boolean active;
    private String createdAt;
}
