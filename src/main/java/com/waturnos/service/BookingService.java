package com.waturnos.service;

import com.waturnos.entity.Booking;
import com.waturnos.entity.BookingStatus;
import com.waturnos.repository.BookingRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getBookingsByTenant(Long tenantId) {
        return bookingRepository.findByTenantTenantId(tenantId);
    }

    public Booking createBooking(Booking booking) {
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    public Optional<Booking> updateStatus(Long bookingId, BookingStatus status) {
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        bookingOpt.ifPresent(b -> {
            b.setStatus(status);
            b.setUpdatedAt(LocalDateTime.now());
            bookingRepository.save(b);
        });
        return bookingOpt;
    }
}