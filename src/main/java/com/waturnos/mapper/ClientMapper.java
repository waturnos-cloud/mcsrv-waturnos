package com.waturnos.mapper;

import com.waturnos.dto.beans.ClientDTO;
import com.waturnos.entity.Client;
import com.waturnos.entity.Organization;

import java.util.List;

import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ClientMapper {
	ClientDTO toDto(Client e);

	Client toEntity(ClientDTO d);

	@Named("orgFromId")
	public static Organization mapOrg(Long id) {
		if (id == null)
			return null;
		Organization o = new Organization();
		o.setId(id);
		return o;
	}

	List<ClientDTO> toDtoList(List<Client> entities);
}
