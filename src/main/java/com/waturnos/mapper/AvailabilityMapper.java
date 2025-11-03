package com.waturnos.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.waturnos.dto.beans.AvailabilityDTO;
import com.waturnos.entity.AvailabilityEntity;

@Mapper(componentModel = "spring")
public interface AvailabilityMapper {
	
	AvailabilityDTO toDto(AvailabilityEntity e);
	
	@Mapping(target = "serviceId", ignore = true)
	AvailabilityEntity toEntity(AvailabilityDTO d);
	
	List<AvailabilityDTO> toDtoList(List<AvailabilityEntity> entities);

    List<AvailabilityEntity> toEntityList(List<AvailabilityDTO> dtos);
}
