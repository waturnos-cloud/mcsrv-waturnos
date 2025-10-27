package com.waturnos.service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class ServiceException extends RuntimeException {
	
	private static final long serialVersionUID = 5914954513605454776L;
	private String errorCode;
	
	public ServiceException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
}
