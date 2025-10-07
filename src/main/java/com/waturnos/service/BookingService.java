package com.waturnos.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.waturnos.entity.Booking;
import com.waturnos.entity.BookingStatus;
import com.waturnos.repository.BookingRepository;
import com.waturnos.security.TenantContext;

/**
 * The Class BookingService.
 */
@Service
public class BookingService {

	/** The booking repository. */
	private final BookingRepository bookingRepository;

	/**
	 * Instantiates a new booking service.
	 *
	 * @param bookingRepository the booking repository
	 */
	public BookingService(BookingRepository bookingRepository) {
		this.bookingRepository = bookingRepository;
	}

	/**
	 * Gets the bookings by tenant.
	 *
	 * @param tenantId the tenant id
	 * @return the bookings by tenant
	 */
	public List<Booking> getBookingsByTenant(Long tenantId) {
		return bookingRepository.findByTenantTenantId(tenantId);
	}

	/**
	 * Creates the booking.
	 *
	 * @param booking the booking
	 * @return the booking
	 */
	public Booking createBooking(Booking booking) {
		booking.setStatus(BookingStatus.PENDING);
		booking.setCreatedAt(LocalDateTime.now());
		return bookingRepository.save(booking);
	}

	/**
	 * Update status.
	 *
	 * @param bookingId the booking id
	 * @param status the status
	 * @return the optional
	 */
	public Optional<Booking> updateStatus(Long bookingId, BookingStatus status) {
		Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
		bookingOpt.ifPresent(b -> {
			b.setStatus(status);
			b.setUpdatedAt(LocalDateTime.now());
			bookingRepository.save(b);
		});
		return bookingOpt;
	}

	/**
	 * Gets the bookings for current tenant.
	 *
	 * @return the bookings for current tenant
	 */
	public List<Booking> getBookingsForCurrentTenant() {
		Long tenantId = TenantContext.getTenantId();
		if (tenantId == null) {
			throw new IllegalStateException("No se pudo determinar el tenant actual");
		}
		return bookingRepository.findByTenantTenantId(tenantId);
	}
}