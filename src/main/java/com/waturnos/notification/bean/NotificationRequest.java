package com.waturnos.notification.bean;

import java.util.Map;

import com.waturnos.notification.enums.NotificationType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder 
public class NotificationRequest {

    /** The subject. */
    private final String subject;

    /** The properties. */
    private final Map<String, String> properties;

    /** The type. */
    private final NotificationType type;

    /** The language. */
    private final String language;

    /** The phone. */
    private final String phone;
    
    /** The email. */
    private final String email;
}