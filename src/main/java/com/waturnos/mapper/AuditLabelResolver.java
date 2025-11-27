package com.waturnos.mapper;

import java.util.Locale;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditLabelResolver {
    private final MessageSource messageSource;

    @Cacheable(cacheNames = "audit:label", key = "T(org.springframework.context.i18n.LocaleContextHolder).getLocale().toLanguageTag().concat('|').concat(#eventCode)")
    public String labelFor(String eventCode) {
        if (eventCode == null || eventCode.isBlank()) return null;
        Locale locale = LocaleContextHolder.getLocale();
        try {
            return messageSource.getMessage("audit.event." + eventCode, null, locale);
        } catch (Exception ex) {
            return eventCode; // fallback to code
        }
    }
}
