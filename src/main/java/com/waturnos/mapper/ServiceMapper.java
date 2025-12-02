package com.waturnos.mapper;

import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.waturnos.dto.beans.LocationDTO;
import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.dto.beans.UserDTO;
import com.waturnos.entity.Location;
import com.waturnos.entity.ServiceEntity;
import com.waturnos.entity.User;
import com.waturnos.repository.AvailabilityRepository;

@Mapper(componentModel = "spring")
public abstract class ServiceMapper {

	@Autowired
	private AvailabilityRepository availabilityRepository;
	
	@Autowired
	private AvailabilityMapper availabilityMapper;

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
	@Mapping(target = "listAvailability", ignore = true)
	public abstract ServiceDTO toDTO(ServiceEntity entity);
	
	/**
	 * After mapping para cargar la disponibilidad.
	 */
	@AfterMapping
	protected void loadAvailability(ServiceEntity entity, @MappingTarget ServiceDTO dto) {
		if (entity != null && entity.getId() != null) {
			dto.setListAvailability(
				availabilityMapper.toDtoList(
					availabilityRepository.findByServiceId(entity.getId())
				)
			);
		}
	}

	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "creator", ignore = true)
	@Mapping(target = "modificator", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	public ServiceEntity toEntity(ServiceDTO dto, boolean full) {
		ServiceEntity serviceEntity = toEntity(dto);
		if (full) {
			serviceEntity.setLocation(mapLocationsToEntity(dto.getLocation()));
			serviceEntity.setUser(toEntity(dto.getUser()));
		}
		return serviceEntity;
	}

	public abstract List<LocationDTO> mapLocations(List<Location> locationEntity);

	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "creator", ignore = true)
	@Mapping(target = "modificator", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "organization", ignore = true)
	public abstract Location mapLocationsToEntity(LocationDTO dto);

	public abstract User toEntity(UserDTO d);

}
