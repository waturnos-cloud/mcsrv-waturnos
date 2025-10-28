package com.waturnos.controller.exceptions;

// Asegúrate de usar tu paquete

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice // Indica que esta clase maneja excepciones de todos los Controllers
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Manager other exceptions
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
    	log.error("ServiceException capturada. URI: {}", request.getDescription(false), ex);
        ErrorResponse errorDetails = new ErrorResponse(
            ErrorCode.GLOBAL_ERROR, 
            "Ocurrió un error inesperado. Consulte los logs.",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Manager ServiceException
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGlobalException(ServiceException ex, WebRequest request) {
    	
        log.error("ServiceException capturada. URI: {}", request.getDescription(false), ex);
        ErrorResponse errorDetails = new ErrorResponse(
            ex.getErrorCode(), 
            "Ocurrió un error inesperado. Consulte los logs.",
            request.getDescription(false)
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}