package com.waturnos.audit;

import com.waturnos.entity.Organization;
import com.waturnos.entity.ServiceEntity;

import lombok.Builder;
import lombok.Data;

/**
 * Context holder for audit information using ThreadLocal.
 * Allows setting contextual information (serviceId, serviceName, etc.) 
 * that will be automatically included in audit entries.
 */
public class AuditContext {

    private static final ThreadLocal<AuditData> CONTEXT = new ThreadLocal<>();

    /**
     * Get current audit context data
     */
    public static AuditData get() {
        AuditData data = CONTEXT.get();
        if (data == null) {
            data = AuditData.builder().build();
            CONTEXT.set(data);
        }
        return data;
    }

    /**
     * Set service ID in current audit context
     */
    public static void setServiceId(Long serviceId) {
        get().setServiceId(serviceId);
    }

    /**
     * Set service name in current audit context
     */
    public static void setServiceName(String serviceName) {
        get().setServiceName(serviceName);
    }

    /**
     * Set both service ID and name
     */
    public static void setService(ServiceEntity serviceEntity) {
        AuditData data = get();
        data.setServiceId(serviceEntity.getId());
        data.setServiceName(serviceEntity.getName());
    }

    /**
     * Clear audit context (important to avoid memory leaks in thread pools)
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Reset context with new data
     */
    public static void reset() {
        CONTEXT.set(AuditData.builder().build());
    }

    /**
     * Set organization ID in current audit context
     */
    public static void setOrganizationId(Long organizationId) {
        get().setOrganizationId(organizationId);
    }

    /**
     * Set organization name in current audit context
     */
    public static void setOrganizationName(String organizationName) {
        get().setOrganizationName(organizationName);
    }

    /**
     * Set both organization ID and name
     */
    public static void setOrganization(Organization organization) {
        AuditData data = get();
        data.setOrganizationId(organization.getId());
        data.setOrganizationName(organization.getName());
    }

    /**
     * Set provider name in current audit context
     */
    public static void setProviderName(String providerName) {
        get().setProviderName(providerName);
    }

    /**
     * Set both provider ID and name from User entity
     */
    public static void setProvider(com.waturnos.entity.User provider) {
        AuditData data = get();
        data.setProviderId(provider.getId());
        data.setProviderName(provider.getFullName());
    }

    /**
     * Set object identifier in current audit context
     */
    public static void setObject(String object) {
        get().setObject(object);
    }

    @Data
    @Builder
    public static class AuditData {
        private Long serviceId;
        private String serviceName;
        private Long organizationId;
        private String organizationName;
        private Long providerId;
        private String providerName;
        private String object;
    }
}
