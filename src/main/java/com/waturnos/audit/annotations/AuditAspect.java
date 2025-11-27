package com.waturnos.audit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for auditing. Monitored with Around advice.
 * The event code is used as a key to lookup the description in messages.properties (audit.event.{eventCode})
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditAspect {
    /**
     * Event code identifier. Used to lookup audit.event.{value} in messages.properties
     */
    String value();
}
