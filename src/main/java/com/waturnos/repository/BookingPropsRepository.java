package com.waturnos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.waturnos.entity.BookingPropsEntity;

public interface BookingPropsRepository extends JpaRepository<BookingPropsEntity, Long> {

    @Query("SELECT bp FROM BookingPropsEntity bp WHERE bp.booking.id = :bookingId")
    List<BookingPropsEntity> findByBookingId(@Param("bookingId") Long bookingId);
}
