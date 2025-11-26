package com.waturnos.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar un turno de un cliente con información completa.
 * Incluye detalles del booking, servicio, provider y organización.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientBookingDTO {

    /** ID del booking */
    private Long bookingId;
    
    /** Fecha formateada, ej: "Jueves, 27 de Noviembre de 2025" */
    private String formattedDate;
    
    /** Hora de inicio del turno */
    private LocalDateTime startTime;
    
    /** Hora de fin del turno */
    private LocalDateTime endTime;
    
    /** Nombre del servicio */
    private String serviceName;
    
    /** Duración del servicio en minutos */
    private Integer serviceDurationMinutes;
    
    /** Nombre completo del provider que ofrece el servicio */
    private String providerName;
    
    /** Nombre de la organización */
    private String organizationName;
    
    /** Estado del booking */
    private String status;
    
    /** Notas del booking (opcional) */
    private String notes;
}
