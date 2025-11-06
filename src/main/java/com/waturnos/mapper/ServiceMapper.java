package com.waturnos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.entity.ServiceEntity;

@Mapper(componentModel = "spring")
public abstract class ServiceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "modificator", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "location", ignore = true)
    public abstract ServiceEntity toEntity(ServiceDTO dto);

    @Mapping(source = "location.id", target = "locationId")
    public abstract ServiceDTO toDTO(ServiceEntity entity);
}

