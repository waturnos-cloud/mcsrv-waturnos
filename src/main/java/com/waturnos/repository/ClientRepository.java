package com.waturnos.repository;
import com.waturnos.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmailAndOrganizationId(String email, Long organizationId);
    List<Client> findByOrganizationId(Long organizationId);
}
