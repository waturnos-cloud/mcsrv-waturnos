package com.waturnos.service;

import com.waturnos.entity.Organization;
import java.util.List;
import java.util.Optional;

public interface OrganizationService {
	List<Organization> findAll();

	Optional<Organization> findById(Long id);

	Organization create(Organization org);

	Organization update(Long id, Organization org);

	void delete(Long id);
}
