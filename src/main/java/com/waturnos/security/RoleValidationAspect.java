package com.waturnos.security;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleValidationAspect {

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        User currentUser = SessionUtil.getCurrentUser();
        UserRole[] requiredRoles = requireRole.value();
        if (currentUser == null) {
            throw new ServiceException(ErrorCode.INSUFFICIENT_PRIVILEGES, "Access denied: user not authenticated.");
        }
        UserRole userRole = currentUser.getRole();
        
        boolean hasPermission = Arrays.stream(requiredRoles)
                                      .anyMatch(role -> role == userRole);

        if (!hasPermission) {
        	throw new ServiceException(ErrorCode.INSUFFICIENT_PRIVILEGES, "Access denied: insufficient permissions.");
        }

        return joinPoint.proceed();
    }
}
