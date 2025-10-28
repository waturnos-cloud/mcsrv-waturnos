package com.waturnos.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;

public class SessionUtil {
	
    public static User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }
    
    public static UserRole getRoleUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user.getRole();
        }
        return null;
    }
    
    public static String getUserName() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof User user) {
            return user.getEmail();
        }
        return null;
    }

}
