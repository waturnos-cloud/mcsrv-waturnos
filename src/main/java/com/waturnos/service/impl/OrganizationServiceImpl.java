package com.waturnos.service.impl;

import com.waturnos.entity.Organization;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.service.OrganizationService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrganizationServiceImpl implements OrganizationService {
	private final OrganizationRepository organizationRepository;

	public OrganizationServiceImpl(OrganizationRepository organizationRepository) {
		this.organizationRepository = organizationRepository;
	}

	@Override
	public List<Organization> findAll() {
		return organizationRepository.findAll();
	}

	@Override
	public Optional<Organization> findById(Long id) {
		return organizationRepository.findById(id);
	}

	@Override
	public Organization create(Organization org) {
		return organizationRepository.save(org);
	}

	@Override
	public Organization update(Long id, Organization org) {
		Organization existing = organizationRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Organization not found"));
		org.setId(existing.getId());
		return organizationRepository.save(org);
	}

	@Override
	public void delete(Long id) {
		if (!organizationRepository.existsById(id))
			throw new EntityNotFoundException("Organization not found");
		organizationRepository.deleteById(id);
	}
}
