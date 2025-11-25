package com.waturnos.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.security.ClientPrincipal;

public class SessionUtil {
	
    /**
     * Get current user (only for User authentication, not Client).
     * 
     * @return User if authenticated as User, null otherwise (including Client authentication)
     */
    public static User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }
    
    /**
     * Get current client principal (only for Client authentication).
     * 
     * @return ClientPrincipal if authenticated as Client, null otherwise
     */
    public static ClientPrincipal getCurrentClient() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof ClientPrincipal client) {
            return client;
        }
        return null;
    }
    
    /**
     * Check if current authentication is a Client.
     * 
     * @return true if authenticated as Client, false otherwise
     */
    public static boolean isClient() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof ClientPrincipal;
    }
    
    /**
     * Check if current authentication is a User.
     * 
     * @return true if authenticated as User, false otherwise
     */
    public static boolean isUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof User;
    }
    
    public static UserRole getRoleUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user.getRole();
        }
        return null;
    }
    
    /**
     * Get organization ID for both User and Client authentication.
     * 
     * @return organizationId from User or Client, null if not authenticated
     */
    public static Long getOrganizationId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user.getIdOrganization();
        }
        if (principal instanceof ClientPrincipal client) {
            return client.getOrganizationId();
        }
        return null;
    }
    
    /**
     * Get username/identifier for both User and Client authentication.
     * 
     * @return fullName for User or identifier for Client, null if not authenticated
     */
    public static String getUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user.getFullName();
        }
        if (principal instanceof ClientPrincipal client) {
            return client.getIdentifier();
        }
        return null;
    }
    
    /**
     * Get client ID (only for Client authentication).
     * 
     * @return clientId if authenticated as Client, null otherwise
     */
    public static Long getClientId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof ClientPrincipal client) {
            return client.getClientId();
        }
        return null;
    }

}
