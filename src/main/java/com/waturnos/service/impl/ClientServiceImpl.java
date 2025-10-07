package com.waturnos.service.impl;
import com.waturnos.entity.Client;
import com.waturnos.repository.ClientRepository;
import com.waturnos.service.ClientService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class ClientServiceImpl implements ClientService {
    private final ClientRepository clientRepository;
    public ClientServiceImpl(ClientRepository clientRepository){this.clientRepository=clientRepository;}
    @Override public List<Client> findByOrganization(Long organizationId){return clientRepository.findByOrganizationId(organizationId);}
    @Override public Client create(Client client){return clientRepository.save(client);}
    @Override public Client update(Long id, Client client){
        Client existing = clientRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Client not found"));
        client.setId(existing.getId()); return clientRepository.save(client);
    }
    @Override public void delete(Long id){
        if(!clientRepository.existsById(id)) throw new EntityNotFoundException("Client not found");
        clientRepository.deleteById(id);
    }
}
