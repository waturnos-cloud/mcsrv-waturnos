package com.waturnos.service.impl;
import com.waturnos.entity.Provider;
import com.waturnos.repository.ProviderRepository;
import com.waturnos.service.ProviderService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class ProviderServiceImpl implements ProviderService {
    private final ProviderRepository providerRepository;
    public ProviderServiceImpl(ProviderRepository providerRepository){this.providerRepository=providerRepository;}
    @Override public List<Provider> findByOrganization(Long organizationId){return providerRepository.findByOrganizationId(organizationId);}
    @Override public Provider create(Provider provider){return providerRepository.save(provider);}
    @Override public Provider update(Long id, Provider provider){
        Provider existing = providerRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Provider not found"));
        provider.setId(existing.getId()); return providerRepository.save(provider);
    }
    @Override public void delete(Long id){
        if(!providerRepository.existsById(id)) throw new EntityNotFoundException("Provider not found");
        providerRepository.deleteById(id);
    }
}
