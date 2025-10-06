package com.waturnos.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;

import com.waturnos.entity.Booking;
import com.waturnos.entity.BookingStatus;
import com.waturnos.service.BookingService;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/tenant/{tenantId}")
    public List<Booking> getByTenant(@PathVariable Long tenantId) {
        return bookingService.getBookingsByTenant(tenantId);
    }

    @PostMapping
    public Booking createBooking(@RequestBody Booking booking) {
        return bookingService.createBooking(booking);
    }

    @PatchMapping("/{id}/status")
    public Optional<Booking> updateStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        return bookingService.updateStatus(id, status);
    }
}