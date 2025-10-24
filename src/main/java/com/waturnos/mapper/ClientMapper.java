package com.waturnos.mapper;

import com.waturnos.dto.ClientDTO;
import com.waturnos.entity.Client;
import com.waturnos.entity.Organization;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientMapper {
	@Mapping(target = "organizationId", source = "organization.id")
	ClientDTO toDto(Client e);

	@Mapping(target = "organization", source = "organizationId", qualifiedByName = "orgFromId")
	Client toEntity(ClientDTO d);

	@Named("orgFromId")
	public static Organization mapOrg(Long id) {
		if (id == null)
			return null;
		Organization o = new Organization();
		o.setId(id);
		return o;
	}
}
