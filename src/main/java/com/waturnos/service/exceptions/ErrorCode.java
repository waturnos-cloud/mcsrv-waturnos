package com.waturnos.service.exceptions;

public enum ErrorCode {

    GLOBAL_ERROR("999", "error.global.unexpected"),
    INSUFFICIENT_PRIVILEGES("998", "error.message.inssuficient.privileges"),
    
    INVALID_USER_OR_PASSWORD("900", "error.message.invalid.userorpassword"),
    
    EMAIL_ALREADY_EXIST_EXCEPTION("1000", "error.message.email.already.exist"),
    USER_NOT_FOUND("1001", "error.message.user.not.found"),
    
    //SERVICE - 1100
    SERVICE_ALREADY_EXIST_EXCEPTION("1100","error.message.service.already.exist"),
    ;

    private final String code;
    private final String messageKey;

    /**
     * Constructor del Enum
     * @param code Código numérico que se expone al cliente.
     * @param messageKey Clave para buscar el mensaje en messages.properties.
     */
    ErrorCode(String code, String messageKey) {
        this.code = code;
        this.messageKey = messageKey;
    }

    // Getters
    public String getCode() {
        return code;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
