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
	LocalDateTime getStartTime();
	LocalDateTime getEndTime();
	BookingStatus getStatus();
	String getNotes();
	String getCancelReason();
	String getClientName();
	String getServiceName();

}
