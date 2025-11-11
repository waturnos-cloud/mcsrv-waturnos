package com.waturnos.service;

import java.time.LocalDate;
import java.util.Set;

public interface UnavailabilityService {
	
	Set<LocalDate> getHolidays();

}
