package com.waturnos.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Location;
import com.waturnos.entity.Organization;
import com.waturnos.entity.Provider;
import com.waturnos.entity.ProviderOrganization;
import com.waturnos.entity.User;
import com.waturnos.enums.OrganizationStatus;
import com.waturnos.enums.UserRole;
import com.waturnos.repository.LocationRepository;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.repository.ProviderOrganizationRepository;
import com.waturnos.repository.ProviderRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.OrganizationService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.utils.DateUtils;
import com.waturnos.utils.SessionUtil;
import com.waturnos.utils.Utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {
	
	private final OrganizationRepository organizationRepository;

	private final UserRepository userRepository;
	
	private final LocationRepository locationRepository;
	
	private final ProviderRepository providerRepository;
	
	private final ProviderOrganizationRepository providerOrganizationRepository;
	
	private final PasswordEncoder passwordEncoder;


	@Override
	public List<Organization> findAll() {
		return organizationRepository.findAll();
	}

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
	 * @param manager the manager
	 * @param b the b
	 * @return the organization
	 */
	@Override
	@RequireRole({UserRole.ADMIN})
	@Transactional(readOnly = false)
	public Organization create(Organization org, User manager, boolean isSimpleOrganization) {
		Optional<User> user = userRepository.findByEmail(manager.getEmail());
		if(user.isPresent()) {
			throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXIST_EXCEPTION, "Email already exists exception");
		}
		org.setActive(true);
		org.setStatus(OrganizationStatus.ACTIVE);
		org.setCreator(SessionUtil.getUserName());
		org.setCreatedAt(DateUtils.getCurrentDateTime());
		Organization organizationDB = organizationRepository.save(org);
		
		manager.setOrganization(organizationDB);
		manager.setRole(UserRole.MANAGER);
		
		String passwordUser = Utils.buildPassword(manager.getFullName(), manager.getPhone());
		log.error("Password inicial: "+ passwordUser);
		
		manager.setPassword(passwordEncoder.encode(passwordUser));
		userRepository.save(manager);
		
		org.getLocations().stream().forEach(l -> l.setOrganization(organizationDB));
		
		locationRepository.saveAll(org.getLocations());
		
		
		if(isSimpleOrganization) {
			createProvider(manager, null, organizationDB);//No tengo bio ni foto, pueden actualizar luego su perfil profesional
		}
		return organizationDB;
		//TODO NOTIFY EMAIL 
	}

	/**
	 * Update basic info.
	 *
	 * @param id the id
	 * @param org the org
	 * @return the organization
	 */
	@Override
	@RequireRole({UserRole.MANAGER, UserRole.ADMIN})
	public Organization updateBasicInfo(Long id, Organization org) {
		Organization organizationDB = organizationRepository.findById(id).orElseThrow(
				() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
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
	@RequireRole({UserRole.MANAGER, UserRole.ADMIN})
	@Transactional(readOnly = false)
	public Organization updateLocations(Long id, List<Location> locations) {
		Organization existing = organizationRepository.findById(id)
	            .orElseThrow(() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
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
	public Organization activateOrDeactivate(Long id, OrganizationStatus organizationStatus) {
		Organization organizationDB = organizationRepository.findById(id).orElseThrow(
				() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
		organizationDB.setStatus(organizationStatus);
		organizationDB.setModificator(SessionUtil.getUserName());
		organizationDB.setUpdatedAt(DateUtils.getCurrentDateTime());
		return organizationRepository.save(organizationDB);
		//TODO si desactiva hay que hacer una notificación masiva de turnos adquiridos informando que revisen los turnos con la organización.
		
	}

	/**
	 * Adds the manager.
	 *
	 * @param id the id
	 * @param manager the manager
	 * @return the user
	 */
	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	@Transactional(readOnly = false)
	public User addManager(Long id, User manager) {
		Organization organizationDB = organizationRepository.findById(id).orElseThrow(
				() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
		
		Optional<User> user = userRepository.findByEmail(manager.getEmail());
		if(user.isPresent()) {
			throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXIST_EXCEPTION, "Email already exists exception");
		}
		
		return createUser(organizationDB, UserRole.MANAGER, manager);
	}

	@Override
	@RequireRole({UserRole.ADMIN, UserRole.MANAGER})
	@Transactional(readOnly = false)
	public Provider addProvider(Long id, Provider provider) {
		Organization organizationDB = organizationRepository.findById(id).orElseThrow(
				() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
		
		Optional<User> existUser = userRepository.findByEmail(provider.getEmail());
		if(existUser.isPresent()) {
			throw new ServiceException(ErrorCode.EMAIL_ALREADY_EXIST_EXCEPTION, "Email already exists exception");
		}
		User userDB = createUser(organizationDB, UserRole.PROVIDER, User.builder()
				.email(provider.getEmail())
				.fullName(provider.getFullName())
				.phone(provider.getPhone())
				.build());
		return createProvider(userDB, provider, organizationDB);
	}
	
	
	/**
	 * Creates the user.
	 *
	 * @param organizationDB the organization DB
	 * @param role the role
	 * @param user the user
	 * @return the user
	 */
	private User createUser(Organization organizationDB, UserRole role, User user) {
		user.setOrganization(organizationDB);
		user.setRole(role);
		user.setCreatedAt(DateUtils.getCurrentDateTime());
		user.setCreator(SessionUtil.getUserName());
		String passwordUser = Utils.buildPassword(user.getFullName(), user.getPhone());
		log.error("Password inicial: "+ passwordUser);
		user.setPassword(passwordEncoder.encode(passwordUser));
		return userRepository.save(user);

	}
	
	/**
	 * Creates the provider.
	 *
	 * @param user the user
	 * @param organizationDB the organization DB
	 */
	private Provider createProvider(User user, Provider provider, Organization organizationDB) {
		Provider providerToCreate = null;
		if(provider == null) {
			providerToCreate = Provider.builder()
				.fullName(user.getFullName())
				.email(user.getEmail())
				.bio(null)
				
				.user(user)//vinculo el manager ya que es el mismo usuario
				.build();
		}else {
			providerToCreate = provider;
		}
		providerToCreate.setCreator(SessionUtil.getUserName());
		providerToCreate.setCreatedAt(DateUtils.getCurrentDateTime());
		providerToCreate.setActive(true);
		providerRepository.save(providerToCreate);
		ProviderOrganization providerOrganization = ProviderOrganization.builder().organization(organizationDB)
				.provider(providerToCreate).createdAt(DateUtils.getCurrentDateTime())
				.creator(SessionUtil.getUserName())
				.startDate(DateUtils.getCurrentDateTime())
				.active(true)
				.build();
		
		providerOrganizationRepository.save(providerOrganization);
		
		return provider;

	}
}
