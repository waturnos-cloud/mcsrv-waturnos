package com.waturnos.service;
import com.waturnos.entity.Provider;
import java.util.List;
public interface ProviderService {
    List<Provider> findByOrganization(Long organizationId);
    Provider create(Provider provider);
    Provider update(Long id, Provider provider);
    void delete(Long id);
}
