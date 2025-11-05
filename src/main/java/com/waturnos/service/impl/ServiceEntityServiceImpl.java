package com.waturnos.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.entity.Booking;
import com.waturnos.entity.ProviderOrganization;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.enums.BookingStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.AvailabilityRepository;
import com.waturnos.repository.LocationRepository;
import com.waturnos.repository.ProviderOrganizationRepository;
import com.waturnos.repository.ServiceRepository;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.BookingService;
import com.waturnos.service.ServiceEntityService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

@Service
public class ServiceEntityServiceImpl implements ServiceEntityService {
	private final ServiceRepository serviceRepository;
	private final AvailabilityRepository availabilityRepository;
	private final BookingService bookingService;
	private final ProviderOrganizationRepository providerOrganizationRepository;
	private final LocationRepository locationRepository;

	public ServiceEntityServiceImpl(ServiceRepository serviceRepository, AvailabilityRepository availabilityRepository, 
			BookingService bookingService, ProviderOrganizationRepository providerOrganizationRepository, LocationRepository locationRepository) {
		this.serviceRepository = serviceRepository;
		this.availabilityRepository = availabilityRepository;
		this.bookingService = bookingService; 
		this.providerOrganizationRepository = providerOrganizationRepository;
		this.locationRepository = locationRepository;
	}

	@Override
	@RequireRole({UserRole.ADMIN,UserRole.MANAGER,UserRole.PROVIDER})
	@Transactional(readOnly = false)
	public ServiceEntity create(ServiceEntity serviceEntity, List<AvailabilityEntity> listAvailability, Long providerId,
			Long organizationId, Long locationId) {
		
		Optional<ProviderOrganization> providerOrganization = providerOrganizationRepository.findByProviderIdAndOrganizationId(providerId, organizationId);
		if(!providerOrganization.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_PROVIDER_ORGANIZATION_EXCEPTION, "Provider-Organization incorrect");
		}
		Optional<ServiceEntity> service = serviceRepository.findByNameAndProviderOrganizationId(serviceEntity.getName(), providerOrganization.get().getId());
		if(service.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_ALREADY_EXIST_EXCEPTION, "Service already exists exception");
		}
		serviceEntity.setProviderOrganization(providerOrganization.get());
		serviceEntity.setLocation(locationRepository.findById(locationId).get());
		ServiceEntity serviceEntityResponse = serviceRepository.save(serviceEntity);
		listAvailability.forEach(av -> {
		    av.setServiceId(serviceEntity.getId());
		    availabilityRepository.save(av);
		});
		generateBookings(serviceEntity, listAvailability);
		return serviceEntityResponse;
	}
	
	private void generateBookings(ServiceEntity service, List<AvailabilityEntity> availabilities) {
	    LocalDate startDate = LocalDate.now();
	    LocalDate endDate = startDate.plusDays(service.getFutureDays());

	    List<Booking> bookings = new ArrayList<>();

	    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
	    	final LocalDate currentDate = date;
	    	DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

	        // Filtrás las disponibilidades que coincidan con ese día
	        availabilities.stream()
	            .filter(a -> a.getDayOfWeek() == dayOfWeek.getValue())
	            .forEach(a -> {
	                LocalTime currentTime = a.getStartTime();
	                while (!currentTime.plusMinutes(service.getDurationMinutes()).isAfter(a.getEndTime())) {
	                    Booking booking = new Booking();
	                    booking.setStartTime(LocalDateTime.of(currentDate, currentTime));
	                    booking.setEndTime(LocalDateTime.of(currentDate, currentTime.plusMinutes(service.getDurationMinutes())));
	                    booking.setStatus(BookingStatus.PENDING);
	                    booking.setService(service);

	                    bookings.add(booking);
	                    currentTime = currentTime.plusMinutes(service.getDurationMinutes());
	                }
	            });
	    }

	    bookingService.create(bookings);
	}
	
	@Override
	@RequireRole({UserRole.ADMIN,UserRole.MANAGER,UserRole.PROVIDER})
	public ServiceEntity findById(Long id) {
		Optional<ServiceEntity> serviceEntity = serviceRepository.findById(id);
		if(!serviceEntity.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_EXCEPTION, "Incorrect service");
		}
		return serviceEntity.get();
	}

	@Override
	@RequireRole({UserRole.ADMIN,UserRole.MANAGER,UserRole.PROVIDER})
	public List<ServiceEntity> findByOrganizationProvider(Long organizationId, Long providerId) {
		Optional<ProviderOrganization> providerOrganization = providerOrganizationRepository.findByProviderIdAndOrganizationId(providerId, organizationId);
		if(!providerOrganization.isPresent()) {
			throw new ServiceException(ErrorCode.SERVICE_PROVIDER_ORGANIZATION_EXCEPTION, "Incorrect Provider-Organization");
		}
		return serviceRepository.findByProviderOrganizationId(providerOrganization.get().getId());
	}
	
	@Override
	@RequireRole({UserRole.ADMIN,UserRole.MANAGER,UserRole.PROVIDER})
	public List<ServiceEntity> findByLocation(Long locationId) {
		return serviceRepository.findByLocationId(locationId);
	}
	
	@Override
	public ServiceEntity update(Long id, ServiceEntity service) {
		ServiceEntity existing = serviceRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Service not found"));
		service.setId(existing.getId());
		return serviceRepository.save(service);
	}
}
