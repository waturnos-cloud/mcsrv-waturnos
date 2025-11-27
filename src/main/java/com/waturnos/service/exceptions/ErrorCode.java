package com.waturnos.service.exceptions;

public enum ErrorCode {

    GLOBAL_ERROR("999", "error.global.unexpected"),
    INSUFFICIENT_PRIVILEGES("998", "error.message.inssuficient.privileges"),
    
    INVALID_USER_OR_PASSWORD("900", "error.message.invalid.userorpassword"),
    
    EMAIL_ALREADY_EXIST_EXCEPTION("1000", "error.message.email.already.exist"),
    USER_NOT_FOUND("1001", "error.message.user.not.found"),
    
    
    ORGANIZATION_NOT_FOUND_EXCEPTION("1002", "error.message.organization.not.found"),
    PASSWORD_RESET_TOKEN_EXPIRED("1003", "error.message.password.reset.expired"),
    PROVIDER_NOT_FOUND("1004", "error.message.provider.not.found"),
    ORGANIZATION_NOT_ACTIVE("1005", "error.message.organization.not.active"),
    
    // SERVICE
    SERVICE_ALREADY_EXIST_EXCEPTION("1100","error.message.service.already.exist"),
    SERVICE_PROVIDER_ORGANIZATION_EXCEPTION("1101","error.message.service.provider.organization.incorrect"),
    SERVICE_EXCEPTION("1102","error.message.service.incorrect"),
    SERVICE_NOT_FOUND("1103","error.message.service.not.found"),
    
    //BOOKING
    BOOKING_NOT_FOUND("1200","error.message.booking.not.found"),
    BOOKING_INVALID_STATUS("1201","error.message.booking.invalid.status"),
    BOOKING_FULL("1202","error.message.booking.full"),
    BOOKING_ALREADY_RESERVED_BYCLIENT("1203","error.message.booking.already.reserved"),
    
    //CLIENT
    CLIENT_NOT_FOUND("1300","error.message.client.not.exist"),
    CLIENT_EXISTS("1301","error.message.client.exist"),
    CLIENT_NOT_EXISTS_IN_ORGANIZATION("1302","error.message.client.not.exist.in.organization"),
    CLIENT_EXISTS_IN_ORGANIZATION("1303","error.message.client.exist.in.organization"),
    
    // WAITLIST
    WAITLIST_NOT_FOUND("1400","error.message.waitlist.not.found"),
    
    // GENERAL
    BAD_REQUEST("400","error.message.bad.request"),
    CONFLICT("409","error.message.conflict"),
    FORBIDDEN("403","error.message.forbidden"),
    ORGANIZATION_NOT_FOUND("1006","error.message.organization.not.found"),
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
