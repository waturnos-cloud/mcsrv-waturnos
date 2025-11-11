package com.waturnos.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.waturnos.dto.response.CountBookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.BookingService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.utils.DateUtils;

import lombok.RequiredArgsConstructor;

/**
 * The Class BookingServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

	/** The booking repository. */
	private final BookingRepository bookingRepository;

	/** The client repository. */
	private final ClientRepository clientRepository;
	
	/** The security access entity. */
	private final SecurityAccessEntity securityAccessEntity;
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


	/**
	 * Creates the.
	 *
	 * @param list the list
	 */
	@Override
	public List<Booking> create(List<Booking> list) {
		return bookingRepository.saveAll(list);
	}

	/**
	 * Update status.
	 *
	 * @param id     the id
	 * @param status the status
	 * @return the booking
	 */
	@Override
	@RequireRole({ UserRole.MANAGER, UserRole.ADMIN, UserRole.PROVIDER })
	public Booking updateStatus(Long id, BookingStatus status) {
		Booking existing = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));
		existing.setStatus(status);
		return bookingRepository.save(existing);
	}

	/**
	 * Update.
	 *
	 * @param id       the id
	 * @param clientId the client id
	 * @return the booking
	 */
	@Override
	public Booking assignBookingToClient(Long id, Long clientId) {

		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		Client client = clientRepository.findById(clientId)
				.orElseThrow(() -> new EntityNotFoundException("Client not found"));

		if (!booking.getStatus().equals(BookingStatus.PENDING)) {
			throw new EntityNotFoundException("Not valid status");
		}

		booking.setUpdatedAt(DateUtils.getCurrentDateTime());
		booking.setStatus(BookingStatus.RESERVED);
		booking.setClient(client);

		return bookingRepository.save(booking);

	}

	/**
	 * Cancel booking.
	 *
	 * @param id     the id
	 * @param reason the reason
	 * @return the booking
	 */
	@Override
	public Booking cancelBooking(Long id, String reason) {

		Booking booking = bookingRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Booking not found"));

		if (!booking.getStatus().equals(BookingStatus.RESERVED)
				&& !booking.getStatus().equals(BookingStatus.CONFIRMED)) {
			throw new EntityNotFoundException("Not valid status");
		}

		// TODO modificator? otro servicio para client y admin/manager?

		booking.setUpdatedAt(DateUtils.getCurrentDateTime());
		booking.setStatus(BookingStatus.CANCELLED);
		booking.setCancelReason(reason);

		return bookingRepository.save(booking);


	}

	/**
	 * Find by service id.
	 *
	 * @param serviceId the service id
	 * @return the list
	 */
	@Override
	public List<Booking> findByServiceId(Long serviceId) {
		return bookingRepository.findByServiceId(serviceId);
	}

	/**
	 * Find bookings for today.
	 *
	 * @return the list
	 */
	@Override
	public List<Booking> findBookingsForToday() {
	    
	    OffsetDateTime now = OffsetDateTime.now();
	    LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
	    LocalDateTime endOfDay = startOfDay.plusDays(1);

	    return bookingRepository.findByStartTimeBetween(startOfDay, endOfDay);
	    
	}

	
	public List<Booking> findBookingsForTodayByProvider(Long providerId) {
	    LocalDate today = LocalDate.now();
	    LocalDateTime startOfDay = today.atStartOfDay();
	    LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
	    return bookingRepository.findByProviderAndStartTimeBetween(providerId, startOfDay, endOfDay);
	}

	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER})
    public List<CountBookingDTO> countBookingsByDateRangeAndProvider(
    		 LocalDate fromDate, 
             LocalDate toDate, 
             Long providerId) {

		securityAccessEntity.controlValidAccessOrganization(providerId);
		
        LocalDate exclusiveEndDate = toDate.plusDays(1); 
        
        List<Object[]> rawCounts = bookingRepository.countBookingsByDayAndStatus(
            fromDate, 
            exclusiveEndDate, 
            providerId
        );

        return rawCountsToDTO(rawCounts);
    }
    
    /**
     * Convierte el resultado crudo del DAO (Fecha, Estado, Conteo) en una 
     * lista de CountBookingDTO, agrupando los estados por fecha.
     */
    private List<CountBookingDTO> rawCountsToDTO(List<Object[]> rawCounts) {
    	Map<String, CountBookingDTO> countsByDate = new LinkedHashMap<>();

        for (Object[] row : rawCounts) {
            
        	java.sql.Date sqlDate = (java.sql.Date) row[0]; 
            LocalDate localDate = sqlDate.toLocalDate(); 
            String dateKey = localDate.format(DATE_FORMATTER);
            
            CountBookingDTO dto = countsByDate.getOrDefault(
                dateKey, CountBookingDTO.builder()
                .date(dateKey)
                .build());
            
            BookingStatus status = BookingStatus.valueOf((String) row[1]);
            Long countLong = (Long) row[2];
            int count = countLong.intValue();
            switch (status) {
                case CANCELLED:
                    dto.setCountCanceled(count);
                    break;
                case RESERVED:
                    dto.setCountReserved(count);
                    break;
                case COMPLETED:
                    dto.setCountCompleted(count);
                    break;
                case PENDING:
                    dto.setCountPending(count);
                    break;
			default:
				break;
            }
            
            countsByDate.put(dateKey, dto);
        }

        return new ArrayList<>(countsByDate.values());
    }
}
