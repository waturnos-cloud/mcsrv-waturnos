package com.waturnos.entity.extended;

import java.time.LocalDateTime;

/**
 * The Interface BookingReminder.
 */
public interface BookingReminder {
    
    /**
     * Gets the full name.
     *
     * @return the full name
     */
    String getFullName();
    
    /**
     * Gets the email.
     *
     * @return the email
     */
    String getEmail();
    
    /**
     * Gets the start time.
     *
     * @return the start time
     */
    LocalDateTime getStartTime();
    
    /**
     * Gets the service name.
     *
     * @return the service name
     */
    String getServiceName();
}