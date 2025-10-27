package com.waturnos.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import com.waturnos.dto.beans.BookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;
import com.waturnos.entity.ServiceEntity;

@Mapper(componentModel = "spring")
public interface BookingMapper {
	@Mappings({ @Mapping(target = "organizationId", source = "organization.id"),
			@Mapping(target = "clientId", source = "client.id"),
			@Mapping(target = "providerId", source = "provider.id"),
			@Mapping(target = "serviceId", source = "service.id") })
	BookingDTO toDto(Booking e);

	@Mappings({ @Mapping(target = "organization", source = "organizationId", qualifiedByName = "orgFromId"),
			@Mapping(target = "client", source = "clientId", qualifiedByName = "clientFromId"),
			@Mapping(target = "provider", source = "providerId", qualifiedByName = "providerFromId"),
			@Mapping(target = "service", source = "serviceId", qualifiedByName = "serviceFromId") })
	Booking toEntity(BookingDTO d);

	@Named("orgFromId")
	public static Organization org(Long id) {
		if (id == null)
			return null;
		Organization o = new Organization();
		o.setId(id);
		return o;
	}

	@Named("clientFromId")
	public static Client client(Long id) {
		if (id == null)
			return null;
		Client c = new Client();
		c.setId(id);
		return c;
	}

	@Named("providerFromId")
	public static Provider prov(Long id) {
		if (id == null)
			return null;
		Provider p = new Provider();
		p.setId(id);
		return p;
	}

	@Named("serviceFromId")
	public static ServiceEntity serv(Long id) {
		if (id == null)
			return null;
		ServiceEntity s = new ServiceEntity();
		s.setId(id);
		return s;
	}
}
