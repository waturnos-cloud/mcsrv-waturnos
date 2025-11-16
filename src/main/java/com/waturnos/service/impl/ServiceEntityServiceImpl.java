package com.waturnos.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.entity.Booking;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.UnavailabilityEntity;
import com.waturnos.entity.User;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.AvailabilityRepository;
import com.waturnos.repository.LocationRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.BookingService;
import com.waturnos.service.ServiceEntityService;
import com.waturnos.service.UnavailabilityService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.service.process.BatchProcessor;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

/**
 * The Class ServiceEntityServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class ServiceEntityServiceImpl implements ServiceEntityService {

	/** The service repository. */
	private final ServiceRepository serviceRepository;

	/** The availability repository. */
	private final AvailabilityRepository availabilityRepository;

	/** The booking service. */
	private final BookingService bookingService;

	/** The location repository. */
	private final LocationRepository locationRepository;

	/** The security access entity. */
	private final SecurityAccessEntity securityAccessEntity;

	/** The user repository. */
	private final UserRepository userRepository;

	/** The unavailability service. */
	private final UnavailabilityService unavailabilityService;

	/** The batch processor. */
	private final BatchProcessor batchProcessor;

	/**
	 * Creates the.
	 *
	 * @param serviceEntity    the service entity
	 * @param listAvailability the list availability
	 * @param userId           the user id
	 * @param locationId       the location id
	 * @param workInHollidays  the work in hollidays
	 * @return the service entity
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	@Transactional(readOnly = false)
	public ServiceEntity create(ServiceEntity serviceEntity, List<AvailabilityEntity> listAvailability, Long userId,
			Long locationId, boolean workInHollidays) {

		Optional<User> userDB = userRepository.findById(userId);
		if (!userDB.isPresent()) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}

		securityAccessEntity.controlValidAccessOrganization(userDB.get().getOrganization().getId());

		Optional<ServiceEntity> service = serviceRepository.findByNameAndUserId(serviceEntity.getName(), userId);
		if (service.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_ALREADY_EXIST_EXCEPTION, "Service already exists exception");
		}
		serviceEntity.setLocation(locationRepository.findById(locationId).get());
		serviceEntity.setUser(userDB.get());
		serviceEntity.setCreator(SessionUtil.getUserName());
		serviceEntity.setCreatedAt(LocalDateTime.now());
		ServiceEntity serviceEntityResponse = serviceRepository.save(serviceEntity);
		listAvailability.forEach(av -> {
			av.setServiceId(serviceEntity.getId());
			availabilityRepository.save(av);
		});

		generateBookings(serviceEntity, listAvailability, workInHollidays ? unavailabilityService.getHolidays() : null);
		return serviceEntityResponse;
	}

	/**
	 * Generate bookings.
	 *
	 * @param service          the service
	 * @param availabilities   the availabilities
	 * @param unavailabilities the unavailabilities
	 */
	private void generateBookings(ServiceEntity service, List<AvailabilityEntity> availabilities,
			Set<LocalDate> unavailabilities) {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(service.getFutureDays());

		List<Booking> bookings = new ArrayList<>();

		for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
			final LocalDate currentDate = date;
			DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

			if (unavailabilities == null || !unavailabilities.contains(currentDate)) {

				// Filtrás las disponibilidades que coincidan con ese día
				availabilities.stream().filter(a -> a.getDayOfWeek() == dayOfWeek.getValue()).forEach(a -> {
					LocalTime currentTime = a.getStartTime();
					while (!currentTime.plusMinutes(service.getDurationMinutes()).isAfter(a.getEndTime())) {
						Booking booking = new Booking();
						booking.setStartTime(LocalDateTime.of(currentDate, currentTime));
						booking.setEndTime(
								LocalDateTime.of(currentDate, currentTime.plusMinutes(service.getDurationMinutes())));
						booking.setStatus(BookingStatus.FREE);
						booking.setService(service);

						bookings.add(booking);
						currentTime = currentTime.plusMinutes(service.getDurationMinutes());
					}
				});
			}
		}

		bookingService.create(bookings);
	}

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the service entity
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	public ServiceEntity findById(Long id) {
		Optional<ServiceEntity> serviceEntity = serviceRepository.findById(id);
		if (!serviceEntity.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		return serviceEntity.get();
	}

	/**
	 * Find by user.
	 *
	 * @param userId the user id
	 * @return the list
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	public List<ServiceEntity> findByUser(Long userId) {
		Optional<User> userDB = userRepository.findById(userId);
		if (!userDB.isPresent()) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}
		securityAccessEntity.controlValidAccessOrganization(userDB.get().getOrganization().getId());
		return serviceRepository.findByUserId(userId);
	}

	/**
	 * Find by location.
	 *
	 * @param locationId the location id
	 * @return the list
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	public List<ServiceEntity> findByLocation(Long locationId) {
		return serviceRepository.findByLocationId(locationId);
	}

	/**
	 * Update.
	 *
	 * @param id      the id
	 * @param service the service
	 * @return the service entity
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	@Transactional(readOnly = false)
	public ServiceEntity update(ServiceEntity service) {

		Optional<ServiceEntity> serviceDB = serviceRepository.findById(service.getId());
		if (!serviceDB.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		if (!serviceDB.get().getUser().getId().equals(service.getUser().getId())) {
			throw new ServiceException(ErrorCode.USER_NOT_FOUND, "User not found");
		}
		serviceDB.get().setUpdatedAt(LocalDateTime.now());
		serviceDB.get().setModificator(SessionUtil.getUserName());
		serviceDB.get().setName(service.getName());
		serviceDB.get().setDescription(service.getDescription());
		serviceDB.get().setAdvancePayment(service.getAdvancePayment());
		serviceDB.get().setLocation(service.getLocation());
		serviceDB.get().setPrice(service.getPrice());

		return serviceRepository.save(service);
	}

	/**
	 * Delete.
	 *
	 * @param serviceId the service id
	 */
	@Override
	@RequireRole({ UserRole.ADMIN, UserRole.MANAGER, UserRole.PROVIDER })
	public void delete(Long serviceId) {
		Optional<ServiceEntity> serviceDB = serviceRepository.findById(serviceId);
		if (!serviceDB.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		batchProcessor.deleteServiceAsync(serviceDB.get().getId(), serviceDB.get().getName(), true);

	}

	/**
	 * Lock calendar.
	 *
	 * @param startDate the start date
	 * @param endDate   the end date
	 * @param serviceId the service id
	 */
	@Override
	@RequireRole({ UserRole.MANAGER, UserRole.PROVIDER })
	public void lockCalendar(LocalDateTime startDate, LocalDateTime endDate, Long serviceId) {
		Optional<ServiceEntity> serviceEntity = serviceRepository.findById(serviceId);
		if (!serviceEntity.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		unavailabilityService.create(UnavailabilityEntity.builder().startDay(startDate.toLocalDate())
				.startTime(startDate.toLocalTime()).endDay(endDate.toLocalDate()).endTime(endDate.toLocalTime())
				.service(ServiceEntity.builder().id(serviceId).build()).build());
		
		batchProcessor.deleteBookings(startDate, endDate, serviceEntity.get());
	}
}
