package com.waturnos.audit;

import java.time.LocalDateTime;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.stereotype.Component;

import com.waturnos.audit.annotations.AuditAspect;
import com.waturnos.entity.Audit;
import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggerAspect {

    private final AuditService auditService;
    private final OrganizationRepository organizationRepository;

    @Around("@annotation(auditAnnotation)")
    public Object aroundAudit(ProceedingJoinPoint pjp, AuditAspect auditAnnotation) throws Throwable {
        LocalDateTime now = LocalDateTime.now();
        long start = System.nanoTime();
        MethodSignature sig = (MethodSignature) pjp.getSignature();

        User currentUser = SessionUtil.getCurrentUser();
        Long orgId = SessionUtil.getOrganizationId();
        String orgName = null;
        if (orgId != null) {
            Organization org = organizationRepository.findById(orgId).orElse(null);
            if (org != null) orgName = org.getName();
        }

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

        String eventCode = auditAnnotation.eventCode();
        if (eventCode == null || eventCode.isBlank()) {
            String prefix = sig.getDeclaringType().getSimpleName();
            String action = sig.getName();
            eventCode = (prefix + "_" + action).toUpperCase().replaceAll("[^A-Z0-9_]", "_");
        }

        Audit.AuditBuilder builder = Audit.builder()
                .date(now)
            .eventCode(eventCode)
            .behavior(auditAnnotation.behavior() == null ? null : auditAnnotation.behavior().trim())
                .username(currentUser != null ? currentUser.getFullName() : null)
                .email(currentUser != null ? currentUser.getEmail() : null)
                .organizationId(orgId)
                .organizationName(orgName)
                .role(currentUser != null && currentUser.getRole() != null ? currentUser.getRole().name() : null)
                .methodSignature(sig.toShortString())
                .ipAddress(ip)
                .userAgent(ua)
                .requestId(requestId);

        try {
            Object result = pjp.proceed();
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            auditService.save(builder.success(Boolean.TRUE).errorMessage(null).durationMs(durationMs).build());
            return result;
        } catch (Throwable ex) {
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            auditService.save(builder.success(Boolean.FALSE).errorMessage(ex.getMessage()).durationMs(durationMs).build());
            throw ex;
        }
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v.split(",")[0].trim();
        }
        return null;
    }
}
