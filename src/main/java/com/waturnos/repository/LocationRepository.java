package com.waturnos.repository;
import com.waturnos.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByOrganizationId(Long organizationId);
}
