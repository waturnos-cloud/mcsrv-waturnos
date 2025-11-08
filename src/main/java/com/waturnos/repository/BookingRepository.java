package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 

import com.waturnos.entity.Booking;


public interface BookingRepository extends JpaRepository<Booking, Long> {
	
	List<Booking> findByClientId(Long clientId);

	List<Booking> findByServiceId(Long serviceId);
	
	@Modifying
	@Query("DELETE FROM Booking b WHERE b.service.id = :serviceId")
	void deleteAllByServiceId(@Param("serviceId") Long serviceId);
	 
}
