package com.waturnos.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.waturnos.entity.UnavailabilityEntity;
import com.waturnos.repository.UnavailabilityRepository;
import com.waturnos.service.UnavailabilityService;

import lombok.RequiredArgsConstructor;

/**
 * The Class UnavailabilityServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class UnavailabilityServiceImpl implements UnavailabilityService {

	/** The unavailability repository. */
	private final UnavailabilityRepository unavailabilityRepository;

	/**
	 * Gets the holidays.
	 *
	 * @return the holidays
	 */
	@Override
	public Set<LocalDate> getHolidays() {

		List<UnavailabilityEntity> unavailabilities = unavailabilityRepository.findByServiceIsNull();
		return unavailabilities.stream().map(UnavailabilityEntity::getStartDay).filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

}
