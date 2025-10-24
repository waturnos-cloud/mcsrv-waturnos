package com.waturnos.controller;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data

/**
 * Instantiates a new api response.
 *
 * @param success the success
 * @param message the message
 * @param data the data
 */
@AllArgsConstructor
public class ApiResponse<T> {
	
	/** The success. */
	private boolean success;
	
	/** The message. */
	private String message;
	
	/** The data. */
	private T data;
}
