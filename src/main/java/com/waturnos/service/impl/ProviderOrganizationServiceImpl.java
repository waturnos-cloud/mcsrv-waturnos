package com.waturnos.service.impl;

import com.waturnos.dto.ProviderOrganizationDTO;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;
import com.waturnos.entity.ProviderOrganization;
import com.waturnos.mapper.ProviderOrganizationMapper;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.ProviderOrganizationRepository;
import com.waturnos.repository.ProviderRepository;
import com.waturnos.service.ProviderOrganizationService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProviderOrganizationServiceImpl implements ProviderOrganizationService {

	private final ProviderOrganizationRepository repository;
	private final ProviderRepository providerRepository;
	private final OrganizationRepository organizationRepository;
	private final ProviderOrganizationMapper mapper;

	public ProviderOrganizationServiceImpl(ProviderOrganizationRepository repository,
			ProviderRepository providerRepository, OrganizationRepository organizationRepository,
			ProviderOrganizationMapper mapper) {
		this.repository = repository;
		this.providerRepository = providerRepository;
		this.organizationRepository = organizationRepository;
		this.mapper = mapper;
	}

	@Override
	public ProviderOrganizationDTO create(ProviderOrganizationDTO dto) {
		Provider provider = providerRepository.findById(dto.getProviderId())
				.orElseThrow(() -> new EntityNotFoundException("Provider not found"));
		Organization org = organizationRepository.findById(dto.getOrganizationId())
				.orElseThrow(() -> new EntityNotFoundException("Organization not found"));

		ProviderOrganization entity = mapper.toEntity(dto, provider, org);
		return mapper.toDto(repository.save(entity));
	}

	@Override
	public List<ProviderOrganizationDTO> findAll() {
		return repository.findAll().stream().map(mapper::toDto).collect(Collectors.toList());
	}

	@Override
	public ProviderOrganizationDTO findById(Long id) {
		return repository.findById(id).map(mapper::toDto)
				.orElseThrow(() -> new EntityNotFoundException("ProviderOrganization not found"));
	}

	@Override
	public List<ProviderOrganizationDTO> findByProvider(Long providerId) {
		return repository.findByProviderId(providerId).stream().map(mapper::toDto).collect(Collectors.toList());
	}

	@Override
	public List<ProviderOrganizationDTO> findByOrganization(Long organizationId) {
		return repository.findByOrganizationId(organizationId).stream().map(mapper::toDto).collect(Collectors.toList());
	}

	@Override
	public ProviderOrganizationDTO update(Long id, ProviderOrganizationDTO dto) {
		ProviderOrganization entity = repository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("ProviderOrganization not found"));
		entity.setStartDate(dto.getStartDate());
		entity.setEndDate(dto.getEndDate());
		entity.setActive(dto.getActive());
		entity.setModificator(dto.getModificator());
		return mapper.toDto(repository.save(entity));
	}

	@Override
	public void delete(Long id) {
		repository.deleteById(id);
	}
}