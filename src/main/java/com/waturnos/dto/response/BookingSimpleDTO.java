package com.waturnos.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.waturnos.enums.BookingStatus;

import lombok.Data;

@Data
public class BookingSimpleDTO {
    private Long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private Boolean isOverbooking;
    private List<BookingClientSimpleDTO> bookingClients;
}
