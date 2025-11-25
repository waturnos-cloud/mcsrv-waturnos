package com.waturnos.audit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods for auditing. Monitored with Around advice.
 * Parameters:
 * - eventCode: business event identifier
 * - behavior: description/context of the action
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuditAspect {
    String eventCode();
    String behavior();
}
