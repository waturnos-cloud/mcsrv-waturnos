package com.waturnos.security;

import org.springframework.stereotype.Service;

import com.waturnos.enums.UserRole;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SecurityAccessEntity {

	/**
	 * Checks for valid access organization.
	 *
	 * @param organizationId the organization id
	 * @return true, if successful
	 */
	public boolean hasValidAccessOrganization(Long organizationId) {
		UserRole role = SessionUtil.getRoleUser();
		if(UserRole.ADMIN.equals(role)) {
			return true;
		}
		if(organizationId.equals(SessionUtil.getOrganizationId())) {
			return true;
		}
		return false;
	}
}
