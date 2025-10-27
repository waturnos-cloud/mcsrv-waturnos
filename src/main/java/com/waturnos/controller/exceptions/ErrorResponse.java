package com.waturnos.controller.exceptions;

public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final String path;
    
    public ErrorResponse(String errorCode,String message, String path) {
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
    }
    
    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}