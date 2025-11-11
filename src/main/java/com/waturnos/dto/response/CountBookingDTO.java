package com.waturnos.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CountBookingDTO {
    private String date; // Formato yyyy-MM-dd para el DatePicker
    private int countCanceled;
    private int countReserved;
    private int countCompleted;
    private int countPending; 

}