package com.waturnos.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * The Class NotificationChannelConfig.
 */
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationChannelConfig {

	// Spring mapeará todas las propiedades que inician con 'notification.channels.'
	/** The channels. */
	// Ejemplo: notification.channels.EMAIL se mapea a "EMAIL"
	private Map<String, String> channels;

	/**
	 * Gets the channels.
	 *
	 * @return the channels
	 */
	public Map<String, String> getChannels() {
		return channels;
	}

	/**
	 * Sets the channels.
	 *
	 * @param channels the channels
	 */
	public void setChannels(Map<String, String> channels) {
		this.channels = channels;
	}
}
