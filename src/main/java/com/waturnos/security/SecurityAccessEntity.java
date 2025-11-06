package com.waturnos.security;

import org.springframework.stereotype.Service;

import com.waturnos.enums.UserRole;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

/**
 * The Class SecurityAccessEntity.
 */
@Service

/**
 * Instantiates a new security access entity.
 */
@RequiredArgsConstructor
public class SecurityAccessEntity {

	/**
	 * Control valid access organization.
	 *
	 * @param organizationId the organization id
	 */
	public void controlValidAccessOrganization(Long organizationId) {
		UserRole role = SessionUtil.getRoleUser();
		if (!UserRole.ADMIN.equals(role) && !(organizationId.equals(SessionUtil.getOrganizationId()))) {
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Cannot admin another organization");
		}
	}
}
