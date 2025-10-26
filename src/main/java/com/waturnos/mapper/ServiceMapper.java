package com.waturnos.mapper;

import com.waturnos.dto.beans.ServiceDTO;
import com.waturnos.entity.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ServiceMapper {
	@Mapping(target = "providerId", source = "provider.id")
	@Mapping(target = "locationId", source = "location.id")
	ServiceDTO toDto(ServiceEntity e);

	@Mapping(target = "provider", source = "providerId", qualifiedByName = "providerFromId")
	@Mapping(target = "location", source = "locationId", qualifiedByName = "locationFromId")
	ServiceEntity toEntity(ServiceDTO d);

	@Named("providerFromId")
	public static Provider mapProv(Long id) {
		if (id == null)
			return null;
		Provider p = new Provider();
		p.setId(id);
		return p;
	}

	@Named("locationFromId")
	public static Location mapLoc(Long id) {
		if (id == null)
			return null;
		Location l = new Location();
		l.setId(id);
		return l;
	}
}
