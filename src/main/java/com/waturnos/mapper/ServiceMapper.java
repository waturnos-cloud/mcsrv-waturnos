package com.waturnos.mapper;

import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.entity.*;
import com.waturnos.repository.LocationRepository;
import com.waturnos.repository.ProviderOrganizationRepository;
import com.waturnos.service.exceptions.EntityNotFoundException;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ServiceMapper {

    @Autowired
    protected ProviderOrganizationRepository providerOrganizationRepository;

    @Autowired
    protected LocationRepository locationRepository;

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "modificator", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "providerOrganization", ignore = true)
    @Mapping(target = "location", ignore = true)
    public abstract ServiceEntity toEntity(ServiceDTO dto);

    @Mapping(source = "providerOrganization.organization.id", target = "organizationId")
    @Mapping(source = "providerOrganization.provider.id", target = "providerId")
    @Mapping(source = "location.id", target = "locationId")
    public abstract ServiceDTO toDTO(ServiceEntity entity);

    @AfterMapping
    protected void loadRelations(ServiceDTO dto, @MappingTarget ServiceEntity entity) {
        if (dto.getOrganizationId() != null) {
        	entity.setProviderOrganization(providerOrganizationRepository
                    .findByProviderIdAndOrganizationId(dto.getProviderId(), dto.getOrganizationId())
                    .orElseThrow(() -> new EntityNotFoundException(
                        "ProviderOrganization not found for providerId=" + dto.getProviderId()
                        + " and organizationId=" + dto.getOrganizationId()
                    )));
        }
        if (dto.getLocationId() != null) {
            entity.setLocation(locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found")));
        }
    }
}

