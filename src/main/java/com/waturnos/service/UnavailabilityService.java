package com.waturnos.service;

import java.time.LocalDate;
import java.util.Set;

import com.waturnos.entity.UnavailabilityEntity;

public interface UnavailabilityService {
	
	/**
	 * Gets the holidays.
	 *
	 * @return the holidays
	 */
	Set<LocalDate> getHolidays();
	
	/**
	 * Creates the.
	 *
	 * @param unavailabilityEntity the unavailability entity
	 * @return the unavailability entity
	 */
	UnavailabilityEntity create(UnavailabilityEntity unavailabilityEntity);

}
