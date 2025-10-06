package com.waturnos.dto;

import com.waturnos.entity.Booking;

public class BookingMapper {
    public static BookingDTO toDto(Booking b) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getBookingId());
        dto.setTenantId(b.getTenant().getTenantId());
        dto.setCustomerId(b.getCustomer().getCustomerId());
        dto.setServiceId(b.getService().getServiceId());
        dto.setStartTime(b.getStartTime());
        dto.setEndTime(b.getEndTime());
        dto.setStatus(b.getStatus().name());
        return dto;
    }
}