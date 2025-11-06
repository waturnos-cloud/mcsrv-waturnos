package com.waturnos.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.repository.LocationRepository;
import com.waturnos.repository.ProviderOrganizationRepository;
import com.waturnos.service.exceptions.EntityNotFoundException;

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
    @Mapping(target = "location", ignore = true)
    public abstract ServiceEntity toEntity(ServiceDTO dto);

    @Mapping(source = "location.id", target = "locationId")
    public abstract ServiceDTO toDTO(ServiceEntity entity);

    @AfterMapping
    public void loadRelations(ServiceDTO dto, @MappingTarget ServiceEntity entity) {
        if (dto.getLocationId() != null) {
            entity.setLocation(locationRepository.findById(dto.getLocationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found")));
        }
    }
}

