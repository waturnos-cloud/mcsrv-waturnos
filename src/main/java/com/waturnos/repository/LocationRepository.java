package com.waturnos.repository;

import com.waturnos.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LocationRepository extends JpaRepository<Location, Long> {
	List<Location> findByOrganizationId(Long organizationId);
}
