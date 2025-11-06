package com.waturnos.service.process;

import com.waturnos.entity.Organization;
import com.waturnos.entity.User;

public interface UserProcess {

	
	/**
	 * Creates the provider.
	 *
	 * @param id the id
	 * @param provider the provider
	 * @return the user
	 */
	User createProvider(Organization organizationDB, User provider);


	/**
	 * Update user.
	 *
	 * @param user the user
	 * @return the user
	 */
	User updateUser(User user);


	/**
	 * Creates the manager.
	 *
	 * @param organizationDB the organization DB
	 * @param manager the manager
	 * @return the user
	 */
	User createManager(Organization organizationDB, User manager);

}
