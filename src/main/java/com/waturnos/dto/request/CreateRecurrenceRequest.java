package com.waturnos.dto.request;

import java.time.LocalDate;

import com.waturnos.enums.RecurrenceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRecurrenceRequest {
    private Long bookingId;
    private RecurrenceType recurrenceType;
    private Integer occurrenceCount; // Para type=COUNT
    private LocalDate endDate;        // Para type=END_DATE
}
