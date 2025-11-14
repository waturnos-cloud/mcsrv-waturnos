/*
 * 
 */
package com.waturnos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.entity.ServiceEntity;

@Mapper(componentModel = "spring")
public abstract class ServiceMapper {

	/**
	 * To entity.
	 *
	 * @param dto the dto
	 * @return the service entity
	 */
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "creator", ignore = true)
	@Mapping(target = "modificator", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "location", ignore = true)
	@Mapping(target = "user", ignore = true)
	public abstract ServiceEntity toEntity(ServiceDTO dto);

	/**
	 * To DTO.
	 *
	 * @param entity the entity
	 * @return the service DTO
	 */
	public abstract ServiceDTO toDTO(ServiceEntity entity);

}
