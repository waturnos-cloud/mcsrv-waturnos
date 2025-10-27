package com.waturnos.dto.request;

import com.waturnos.dto.beans.OrganizationDTO;
import com.waturnos.dto.beans.UserDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Instantiates a new login request.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CreateOrganization {
	private OrganizationDTO organization;
	private boolean simpleOrganization;
	private UserDTO manager;
	

}
