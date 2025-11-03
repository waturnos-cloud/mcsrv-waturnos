package com.waturnos.notification.service;

import com.waturnos.notification.bean.NotificationRequest;

/**
 * The Interface NotificationService.
 */
public interface NotificationService {
	
	/**
	 * Gets the channel name.
	 *
	 * @return the channel name
	 */
	String getChannelName();
    
    /**
     * Send notification.
     *
     * @param request the request
     */
    void sendNotification(NotificationRequest request);

}
