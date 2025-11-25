package com.waturnos.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Location;
import com.waturnos.audit.annotations.AuditAspect;
import com.waturnos.entity.Organization;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.LocationRepository;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.OrganizationService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.service.process.UserProcess;
import com.waturnos.utils.DateUtils;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizationServiceImpl implements OrganizationService {
	
	private final OrganizationRepository organizationRepository;

	private final UserRepository userRepository;
	
	private final LocationRepository locationRepository;
	
	private final UserProcess userProcess;
	
	@Override
	public Optional<Organization> findById(Long id) {
		return organizationRepository.findById(id);
	}



	@Override
	public void delete(Long id) {
		if (!organizationRepository.existsById(id))
			throw new EntityNotFoundException("Organization not found");
		organizationRepository.deleteById(id);
	}

	/**
	 * Creates the.
	 *
	 * @param org the org
	 * @param user the user
	 * @param b the b
	 * @return the organization
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.SELLER})
	@Transactional(readOnly = false)
	@AuditAspect(eventCode = "ORG_CREATE", behavior = "Creación de organización")
	public Organization create(Organization org, User user) {
		
		Optional<User> userDB = userRepository.findByEmail(user.getEmail());
		if(userDB.isPresent()) {
			throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXIST_EXCEPTION, "Email already exists exception");
		}
		org.setActive(true);
		org.setStatus(OrganizationStatus.ACTIVE);
		org.setCreator(SessionUtil.getUserName());
		org.setCreatedAt(DateUtils.getCurrentDateTime());
		Organization organizationDB = organizationRepository.save(org);
		if(organizationDB.isSimpleOrganization()) {
			userProcess.createProvider(organizationDB,user);
		}
		else{
			userProcess.createManager(organizationDB,user);
		}
		org.getLocations().stream().forEach(l -> l.setOrganization(organizationDB));
		locationRepository.saveAll(org.getLocations());
		return organizationDB;
		
	}

	/**
	 * Update basic info.
	 *
	 * @param id the id
	 * @param org the org
	 * @return the organization
	 */
	@Override
	@RequireRole({UserRole.MANAGER, UserRole.ADMIN, UserRole.PROVIDER, UserRole.SELLER })
	@AuditAspect(eventCode = "ORG_UPDATE_BASIC", behavior = "Actualización datos básicos organización")
	public Organization updateBasicInfo(Organization org) {
		Organization organizationDB = organizationRepository.findById(org.getId()).orElseThrow(
				() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
		if(SessionUtil.getRoleUser() == UserRole.PROVIDER && !organizationDB.isSimpleOrganization()) {
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Role provider only modified if simple organization");
		}
		organizationDB.setLogoUrl(org.getLogoUrl());
		organizationDB.setName(org.getName());
		organizationDB.setType(org.getType());
		organizationDB.setModificator(SessionUtil.getUserName());
		organizationDB.setUpdatedAt(DateUtils.getCurrentDateTime());
		return organizationRepository.save(organizationDB);

	}

	/**
	 * Update locations.
	 *
	 * @param id the id
	 * @param locations the locations
	 * @return the organization
	 */
	@Override
	@RequireRole({UserRole.MANAGER, UserRole.ADMIN, UserRole.PROVIDER, UserRole.SELLER})
	@Transactional(readOnly = false)
	@AuditAspect(eventCode = "ORG_UPDATE_LOCATIONS", behavior = "Actualización de locations organización")
	public Organization updateLocations(Long id, List<Location> locations) {
		Organization existing = organizationRepository.findById(id)
	            .orElseThrow(() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
		if(SessionUtil.getRoleUser() == UserRole.PROVIDER && !existing.isSimpleOrganization()) {
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Role provider only modified if simple organization");
		}
		List<Location> deleteLocations = new ArrayList<>();
		List<Location> upsertLocations = syncLocations(existing, locations, deleteLocations);
		locationRepository.saveAll(upsertLocations);
		locationRepository.deleteAll(deleteLocations);
	    return existing;
	  //TODO si se elimina debemos hacer algo con los turnos o setearle la principal o notificar algo.
	}

	/**
	 * Sync locations.
	 *
	 * @param existingOrg the existing org
	 * @param incomingLocations the incoming locations
	 * @param deleteLocations 
	 * @return 
	 */
	private List<Location> syncLocations(Organization existingOrg, List<Location> incomingLocations, List<Location> deleteLocations) {
	    
	    List<Location> existingLocations = existingOrg.getLocations();
	    
	    Map<Long, Location> incomingMap = incomingLocations.stream()
	        .filter(location -> location.getId() != null) 
	        .collect(Collectors.toMap(Location::getId, Function.identity()));

	    // --- 1. Eliminar (orphanRemoval) y Actualizar Existentes ---
	    // Usamos removeIf para manejar las eliminaciones de manera eficiente.
	    existingLocations.removeIf(existingLocation -> {
	        // Buscamos la Location entrante que corresponde por ID
	        Location updatedLocation = incomingMap.get(existingLocation.getId());
	        
	        if (updatedLocation == null) {
	            // Si la Location existente NO está en la lista entrante (incomingMap),
	            // significa que debe ser ELIMINADA (devuelve 'true').
	        	deleteLocations.add(existingLocation);
	            return true; 
	        } else {
	            // Si SÍ está, ACTUALIZAMOS in-place
	            // Copiamos las propiedades del bean entrante sobre el bean existente.
	            // Esto es necesario porque el bean existente está bajo el control de JPA.
	            // NOTA: Debes implementar un método de copia (ej. en el bean o con otro mapper).
	            // Si no usas un mapper, tienes que copiar manualmente:
	            existingLocation.setName(updatedLocation.getName());
	            existingLocation.setAddress(updatedLocation.getAddress());
	            existingLocation.setPhone(updatedLocation.getPhone());
	            existingLocation.setEmail(updatedLocation.getEmail());
	            existingLocation.setLatitude(updatedLocation.getLatitude());
	            existingLocation.setMain(updatedLocation.getMain());
	            existingLocation.setLongitude(updatedLocation.getLongitude());
	            existingLocation.setModificator(SessionUtil.getUserName());
	            existingLocation.setUpdatedAt(DateUtils.getCurrentDateTime());
	            incomingMap.remove(existingLocation.getId()); 
	            // NO eliminar el elemento existente (devuelve 'false').
	            return false; 
	        }
	    });

	    // --- 2. Insertar Nuevas (sin ID) ---
	    incomingLocations.stream()
	        // Filtramos las Locations que NO tienen ID (son nuevas)
	        .filter(newLocation -> newLocation.getId() == null)
	        .forEach(newLocation -> {
	            // Aseguramos la doble vía (bidireccionalidad)
	            newLocation.setOrganization(existingOrg);
	            // Agregamos a la colección controlada por JPA.
	            newLocation.setCreator(SessionUtil.getUserName());
	            newLocation.setUpdatedAt(DateUtils.getCurrentDateTime());
	            existingLocations.add(newLocation);
	        });
	    return existingLocations;
	    // NOTA: Si el paso de actualización manual de propiedades es complejo, 
	    // puedes usar MapStruct para eso (ej. 'locationMapper.update(updatedLocation, existingLocation)').
	}

	@Override
	@RequireRole({UserRole.ADMIN})
	@Transactional(readOnly = false)
	@AuditAspect(eventCode = "ORG_STATUS_CHANGE", behavior = "Cambio de estado organización")
	public Organization activateOrDeactivate(Long id, OrganizationStatus organizationStatus) {
		Organization organizationDB = organizationRepository.findById(id).orElseThrow(
				() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
		organizationDB.setStatus(organizationStatus);
		organizationDB.setModificator(SessionUtil.getUserName());
		organizationDB.setUpdatedAt(DateUtils.getCurrentDateTime());
		return organizationRepository.save(organizationDB);
		//TODO si desactiva hay que hacer una notificación masiva de turnos adquiridos informando que revisen los turnos con la organización.
		
	}

	@Override
	public List<Organization> findAll() {
		return organizationRepository.findByStatusOrderByNameAsc(OrganizationStatus.ACTIVE); 
	}

	/**
	 * Find all.
	 *
	 * @param authentication the authentication
	 * @return the list
	 */
	@Override
	@RequireRole(value = {UserRole.ADMIN, UserRole.MANAGER, UserRole.SELLER})
	public List<Organization> findAll(Authentication authentication) {
		String principalRole = authentication.getAuthorities().stream()
	            .map(GrantedAuthority::getAuthority)
	            .findFirst() // Se asume un rol principal para la decisión
	            .orElse("");
		
		// 3. Control de Roles (Lógica de acceso)
        return switch (principalRole) {
            case "ROLE_ADMIN" -> {
                // El ADMIN tiene la menor restricción
            	Sort sortByOrganizationName = Sort.by(Sort.Direction.ASC, "name");
                yield organizationRepository.findAll(sortByOrganizationName);
            }
            case "ROLE_MANAGER" -> {
                // El MANAGER tiene restricción por su organización
            	User user = userRepository.findById(SessionUtil.getCurrentUser().getId()).get();
                yield Arrays.asList(user.getOrganization());
            }
            default -> {
                throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Acceso no permitido");
            }
        };
	}
}
