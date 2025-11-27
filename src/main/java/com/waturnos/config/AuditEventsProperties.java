package com.waturnos.config;

import java.util.Collections;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "audit.events")
public class AuditEventsProperties {
    private List<String> admin = Collections.emptyList();
    private List<String> manager = Collections.emptyList();
    private List<String> provider = Collections.emptyList();
}
