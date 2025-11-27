package com.waturnos.dto.request;

import java.time.LocalDate;
import java.time.LocalTime;

import com.waturnos.enums.WaitlistType;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWaitlistRequest {
    @NotNull(message = "clientId es requerido")
    private Long clientId;
    
    @NotNull(message = "serviceId es requerido")
    private Long serviceId;
    
    @NotNull(message = "providerId es requerido")
    private Long providerId;
    
    @NotNull(message = "organizationId es requerido")
    private Long organizationId;
    
    @NotNull(message = "type es requerido")
    private WaitlistType type;
    
    // REQUERIDO si type === SPECIFIC
    private Long specificBookingId;
    
    @NotNull(message = "date es requerido")
    private LocalDate date;
    
    @NotNull(message = "timeFrom es requerido")
    private LocalTime timeFrom;
    
    @NotNull(message = "timeTo es requerido")
    private LocalTime timeTo;
}
