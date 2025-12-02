package com.waturnos.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.waturnos.dto.beans.AvailabilityDTO;
import com.waturnos.entity.AvailabilityEntity;
import com.waturnos.utils.DateUtils;

@Mapper(componentModel = "spring", imports = DateUtils.class)
public interface AvailabilityMapper {
	
	// Entity → DTO: convertir de Java/BD (1-7) a JS (0-6)
	@Mapping(target = "dayOfWeek", expression = "java(DateUtils.convertDayOfWeekToJs(e.getDayOfWeek()))")
	AvailabilityDTO toDto(AvailabilityEntity e);
	
	// DTO → Entity: convertir de JS (0-6) a Java/BD (1-7)
	@Mapping(target = "serviceId", ignore = true)
	@Mapping(target = "dayOfWeek", expression = "java(DateUtils.convertJsToDayOfWeek(d.getDayOfWeek()))")
	AvailabilityEntity toEntity(AvailabilityDTO d);
	
	List<AvailabilityDTO> toDtoList(List<AvailabilityEntity> entities);

    List<AvailabilityEntity> toEntityList(List<AvailabilityDTO> dtos);
}
