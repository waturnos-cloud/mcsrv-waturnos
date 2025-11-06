package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.Booking;
import com.waturnos.enums.BookingStatus;


public interface BookingRepository extends JpaRepository<Booking, Long> {
	
	List<Booking> findByClientId(Long clientId);

	List<Booking> findByStatus(BookingStatus status);
}
