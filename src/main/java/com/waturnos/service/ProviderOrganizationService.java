package com.waturnos.service;

import com.waturnos.dto.ProviderOrganizationDTO;
import java.util.List;

public interface ProviderOrganizationService {
	ProviderOrganizationDTO create(ProviderOrganizationDTO dto);

	List<ProviderOrganizationDTO> findAll();

	ProviderOrganizationDTO findById(Long id);

	List<ProviderOrganizationDTO> findByProvider(Long providerId);

	List<ProviderOrganizationDTO> findByOrganization(Long organizationId);

	ProviderOrganizationDTO update(Long id, ProviderOrganizationDTO dto);

	void delete(Long id);
}