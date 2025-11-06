package com.waturnos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.waturnos.entity.User;
import com.waturnos.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {
	
	/**
	 * Find by email.
	 *
	 * @param email the email
	 * @return the optional
	 */
	Optional<User> findByEmail(String email);

	/**
	 * Find by organization id order by full name asc.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	List<User> findByOrganizationIdOrderByFullNameAsc(Long organizationId);
	
	/**
	 * Find by organization id and user role order by full name asc.
	 *
	 * @param organizationId the organization id
	 * @param userRole the user role
	 * @return the list
	 */
	List<User> findByOrganizationIdAndRoleOrderByFullNameAsc(Long organizationId, UserRole userRole);
}
