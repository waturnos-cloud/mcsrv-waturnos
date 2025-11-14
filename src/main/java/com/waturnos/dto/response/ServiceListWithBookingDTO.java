package com.waturnos.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.waturnos.enums.BookingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ServiceListWithBookingDTO {
	private String name; 
    private Long id;     
    private List<BookingExtendedDTO> list;
    
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor 
    public static class BookingExtendedDTO {
        private Long id;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private BookingStatus status;
        private String clientName; 
        private String cancelReason;
        private String notes;
    }
}
