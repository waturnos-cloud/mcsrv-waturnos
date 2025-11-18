package com.waturnos.entity.extended;

import java.time.LocalDateTime;

import com.waturnos.enums.BookingStatus;

/**
 * The Interface BookingSummaryDetail.
 */
public interface BookingSummaryDetail {
	
	Long getId();
	Long getClientId();
	Long getServiceId();
	Integer getServiceCapacity();
	LocalDateTime getStartTime();
	LocalDateTime getEndTime();
	BookingStatus getStatus();
	Integer getFreeSlots();
	String getNotes();
	String getCancelReason();
	String getClientName();
	String getServiceName();

}
