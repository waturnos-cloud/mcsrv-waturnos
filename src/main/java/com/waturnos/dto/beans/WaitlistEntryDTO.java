package com.waturnos.dto.beans;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.waturnos.enums.WaitlistStatus;
import com.waturnos.enums.WaitlistType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistEntryDTO {
    private Long id;
    
    // Referencias con nombres para mostrar en UI
    private Long clientId;
    private Long serviceId;
    private String serviceName;
    private Long providerId;
    private String providerName;
    private Long organizationId;
    
    // Configuración de la espera
    private WaitlistType type;
    private Long specificBookingId;  // Solo si type === SPECIFIC
    
    // Rango de tiempo deseado
    private LocalDate date;
    private LocalTime timeFrom;
    private LocalTime timeTo;
    
    // Estado
    private Integer position;    // Posición en la cola (1 = primero)
    private WaitlistStatus status;
    private Integer expirationMinutes;
    
    // Timestamps
    private LocalDateTime notifiedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
