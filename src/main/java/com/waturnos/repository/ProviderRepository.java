package com.waturnos.repository;
import com.waturnos.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface ProviderRepository extends JpaRepository<Provider, Long> {
    List<Provider> findByOrganizationId(Long organizationId);
}
