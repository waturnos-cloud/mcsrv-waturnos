package com.waturnos.schedule;

public interface ScheduledTasks {
	
	/**
	 * Remember booking to users.
	 */
    void rememberBookingToUsers();
    
    /**
     * Adds the booking next day.
     */
    void addBookingNextDay();
    
    /**
     * Completes reserved bookings at end of day.
     * Converts RESERVED to COMPLETED and RESERVED_AFTER_CANCEL to COMPLETED_AFTER_CANCEL.
     */
    void completeReservedBookings();

}
