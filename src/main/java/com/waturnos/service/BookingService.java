package com.waturnos.service;
import com.waturnos.entity.Booking;
import com.waturnos.enums.BookingStatus;
import java.util.List;
public interface BookingService {
    List<Booking> findByOrganization(Long organizationId);
    List<Booking> findByStatus(BookingStatus status);
    Booking create(Booking booking);
    Booking updateStatus(Long id, BookingStatus status);
}
