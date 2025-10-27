package com.waturnos.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.waturnos.entity.Provider;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.ProviderRepository;
import com.waturnos.service.ProviderService;
import com.waturnos.service.exceptions.EntityNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProviderServiceImpl implements ProviderService {
	private final ProviderRepository providerRepository;
	
	private final OrganizationRepository organizationRepository;


	@Override
	public List<Provider> findByOrganization(Long organizationId) {
		return organizationRepository.findById(organizationId).get().getProviders();
	}

	@Override
	public Provider create(Provider provider) {
		return providerRepository.save(provider);
	}

	@Override
	public Provider update(Long id, Provider provider) {
		Provider existing = providerRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Provider not found"));
		provider.setId(existing.getId());
		return providerRepository.save(provider);
	}

	@Override
	public void delete(Long id) {
		if (!providerRepository.existsById(id))
			throw new EntityNotFoundException("Provider not found");
		providerRepository.deleteById(id);
	}
}
