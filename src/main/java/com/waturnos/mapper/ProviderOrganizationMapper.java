package com.waturnos.mapper;

import org.springframework.stereotype.Component;

import com.waturnos.dto.ProviderOrganizationDTO;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;
import com.waturnos.entity.ProviderOrganization;

@Component
public class ProviderOrganizationMapper {

	public ProviderOrganizationDTO toDto(ProviderOrganization entity) {
		if (entity == null)
			return null;

		ProviderOrganizationDTO dto = new ProviderOrganizationDTO();
		dto.setId(entity.getId());
		dto.setProviderId(entity.getProvider() != null ? entity.getProvider().getId() : null);
		dto.setOrganizationId(entity.getOrganization() != null ? entity.getOrganization().getId() : null);
		dto.setStartDate(entity.getStartDate());
		dto.setEndDate(entity.getEndDate());
		dto.setActive(entity.getActive());
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setUpdatedAt(entity.getUpdatedAt());
		dto.setCreator(entity.getCreator());
		dto.setModificator(entity.getModificator());
		return dto;
	}

	public ProviderOrganization toEntity(ProviderOrganizationDTO dto, Provider provider, Organization organization) {
		if (dto == null)
			return null;

		return ProviderOrganization.builder().id(dto.getId()).provider(provider).organization(organization)
				.startDate(dto.getStartDate()).endDate(dto.getEndDate()).active(dto.getActive())
				.createdAt(dto.getCreatedAt()).updatedAt(dto.getUpdatedAt()).creator(dto.getCreator())
				.modificator(dto.getModificator()).build();
	}
}