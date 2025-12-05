package com.waturnos.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckRecurrenceResponse {
    private Boolean canBeRecurrent;
    private List<String> conflictingDates; // Fechas con turnos ocupados
    private List<String> availableDates;   // Fechas disponibles
    private Integer totalFutureSlots;       // Total de slots futuros encontrados
    private Integer availableSlots;         // Slots disponibles
    private Integer conflictingSlots;       // Slots ocupados
    private String message;
}
