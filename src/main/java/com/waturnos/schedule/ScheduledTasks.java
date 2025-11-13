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

}
