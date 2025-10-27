package com.waturnos.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.LocationRepository;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.ProviderRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.OrganizationService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.utils.DateUtils;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
	
	private final OrganizationRepository organizationRepository;

	private final UserRepository userRepository;
	
	private final LocationRepository locationRepository;
	
	private final ProviderRepository providerRepository;


	@Override
	public List<Organization> findAll() {
		return organizationRepository.findAll();
	}

	@Override
	public Optional<Organization> findById(Long id) {
		return organizationRepository.findById(id);
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

	/**
	 * Creates the.
	 *
	 * @param org the org
	 * @param manager the manager
	 * @param b the b
	 * @return the organization
	 */
	@Override
	@RequireRole({UserRole.ADMIN})
	@Transactional(readOnly = false)
	public Organization create(Organization org, User manager, boolean isSimpleOrganization) {
		Optional<User> user = userRepository.findByEmail(manager.getEmail());
		if(user.isPresent()) {
			throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXIST_EXCEPTION, "Email already exists exception");
		}
		org.setActive(true);
		org.setStatus(OrganizationStatus.ACTIVE);
		org.setCreator(SessionUtil.getUserName());
		org.setCreatedAt(DateUtils.getCurrentDateTime());
		Organization organizationDB = organizationRepository.save(org);
		
		manager.setOrganization(organizationDB);
		
		userRepository.save(manager);
		
		org.getLocations().stream().forEach(l -> l.setOrganization(organizationDB));
		
		locationRepository.saveAll(org.getLocations());
		
		
		if(isSimpleOrganization) {
			Provider provider = Provider.builder().active(true).creator(SessionUtil.getUserName())
					.fullName(manager.getFullName())
					.email(manager.getEmail())
					.organizations(Arrays.asList(organizationDB))
					.createdAt(DateUtils.getCurrentDateTime()).build();
			providerRepository.save(provider);
		}
		
		return organizationDB;
		//TODO NOTIFY EMAIL 
	}
}
