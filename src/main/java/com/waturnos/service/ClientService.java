package com.waturnos.service;

import com.waturnos.entity.Client;
import java.util.List;

public interface ClientService {
	List<Client> findByOrganization(Long organizationId);

	Client create(Client client);

	Client update(Long id, Client client);

	void delete(Long id);
}
