package com.waturnos.audit;

import java.time.LocalDateTime;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.waturnos.audit.annotations.AuditAspect;
import com.waturnos.entity.Audit;
import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class AuditLoggerAspect.
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLoggerAspect {

    /** The audit service. */
    private final AuditService auditService;
    
    /** The organization repository. */
    private final OrganizationRepository organizationRepository;

    /**
     * Around audit.
     *
     * @param pjp the pjp
     * @param auditAnnotation the audit annotation
     * @return the object
     * @throws Throwable the throwable
     */
    @Around("@annotation(auditAnnotation)")
    public Object aroundAudit(ProceedingJoinPoint pjp, AuditAspect auditAnnotation) throws Throwable {
        LocalDateTime now = LocalDateTime.now();
        long start = System.nanoTime();
        MethodSignature sig = (MethodSignature) pjp.getSignature();

        User currentUser = SessionUtil.getCurrentUser();
        
        // Obtener datos de la request ANTES del proceed
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = null;
        String ua = null;
        String requestId = null;
        if (attrs != null) {
            var req = attrs.getRequest();
            ip = firstNonEmpty(req.getHeader("X-Forwarded-For"), req.getRemoteAddr());
            ua = req.getHeader("User-Agent");
            requestId = firstNonEmpty(req.getHeader("X-Request-Id"), UUID.randomUUID().toString());
        } else {
            requestId = UUID.randomUUID().toString();
        }

        // Obtener event code desde la anotación
        String event = auditAnnotation.value();
        if (event == null || event.isBlank()) {
            String prefix = sig.getDeclaringType().getSimpleName();
            String action = sig.getName();
            event = (prefix + "_" + action).toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        }

        try {
            // Ejecutar el método - aquí se popula el AuditContext
            Object result = pjp.proceed();
            
            // DESPUÉS del proceed, obtener datos del contexto de auditoría
            AuditContext.AuditData contextData = AuditContext.get();
            
            // Usar organizationId/organizationName del contexto si están disponibles,
            // sino usar de SessionUtil (para casos donde el contexto no se popula)
            Long orgId = contextData.getOrganizationId();
            String orgName = contextData.getOrganizationName();
            
            if (orgId == null) {
                orgId = SessionUtil.getOrganizationId();
                if (orgId != null) {
                    Organization org = organizationRepository.findById(orgId).orElse(null);
                    if (org != null) {
                        orgName = org.getName();
                    }
                }
            }
            
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            
            Audit audit = Audit.builder()
                    .date(now)
                    .event(event)
                    .username(currentUser != null ? currentUser.getFullName() : null)
                    .email(currentUser != null ? currentUser.getEmail() : null)
                    .organizationId(orgId)
                    .organizationName(orgName)
                    .role(currentUser != null && currentUser.getRole() != null ? currentUser.getRole().name() : null)
                    .methodSignature(sig.toShortString())
                    .ipAddress(ip)
                    .userAgent(ua)
                    .requestId(requestId)
                    .serviceId(contextData.getServiceId())
                    .serviceName(contextData.getServiceName())
                    .success(Boolean.TRUE)
                    .errorMessage(null)
                    .durationMs(durationMs)
                    .build();
            
            auditService.save(audit);
            return result;
            
        } catch (Throwable ex) {
            // En caso de error, intentar obtener contexto (puede no estar completo)
            AuditContext.AuditData contextData = AuditContext.get();
            
            Long orgId = contextData.getOrganizationId();
            String orgName = contextData.getOrganizationName();
            
            if (orgId == null) {
                orgId = SessionUtil.getOrganizationId();
                if (orgId != null) {
                    Organization org = organizationRepository.findById(orgId).orElse(null);
                    if (org != null) {
                        orgName = org.getName();
                    }
                }
            }
            
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            
            Audit audit = Audit.builder()
                    .date(now)
                    .event(event)
                    .username(currentUser != null ? currentUser.getFullName() : null)
                    .email(currentUser != null ? currentUser.getEmail() : null)
                    .organizationId(orgId)
                    .organizationName(orgName)
                    .role(currentUser != null && currentUser.getRole() != null ? currentUser.getRole().name() : null)
                    .methodSignature(sig.toShortString())
                    .ipAddress(ip)
                    .userAgent(ua)
                    .requestId(requestId)
                    .serviceId(contextData.getServiceId())
                    .serviceName(contextData.getServiceName())
                    .success(Boolean.FALSE)
                    .errorMessage(ex.getMessage())
                    .durationMs(durationMs)
                    .build();
            
            auditService.save(audit);
            throw ex;
            
        } finally {
            AuditContext.clear();
        }
    }

    /**
     * First non empty.
     *
     * @param values the values
     * @return the string
     */
    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v.split(",")[0].trim();
        }
        return null;
    }
}
