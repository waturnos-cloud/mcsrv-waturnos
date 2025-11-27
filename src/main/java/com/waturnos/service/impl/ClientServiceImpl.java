package com.waturnos.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.waturnos.audit.AuditContext;
import com.waturnos.audit.annotations.AuditAspect;
import com.waturnos.dto.beans.ClientNotificationDTO;
import com.waturnos.dto.response.ClientBookingDTO;
import com.waturnos.entity.Booking;
import com.waturnos.entity.Client;
import com.waturnos.entity.ClientOrganization;
import com.waturnos.entity.Organization;
import com.waturnos.enums.UserRole;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.BookingRepository;
import com.waturnos.repository.ClientOrganizationRepository;
import com.waturnos.repository.ClientRepository;
import com.waturnos.repository.OrganizationRepository;
import com.waturnos.security.SecurityAccessEntity;
import com.waturnos.security.annotations.RequireRole;
import com.waturnos.service.ClientService;
import com.waturnos.service.exceptions.EntityNotFoundException;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;
import com.waturnos.utils.DateUtils;
import com.waturnos.utils.SessionUtil;

import lombok.RequiredArgsConstructor;

/**
 * The Class ClientServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

	/** The client repository. */
	private final ClientRepository clientRepository;
	
	/** The client organization repository. */
	private final ClientOrganizationRepository clientOrganizationRepository;
	
	/** The organization repository. */
	private final OrganizationRepository organizationRepository;
	
	/** The booking repository. */
	private final BookingRepository bookingRepository;
	
	/** The security access entity. */
	private final SecurityAccessEntity securityAccessEntity;
	
	/** The notification factory. */
	private final NotificationFactory notificationFactory;

	@Value("${app.notification.HOME:}")
	private String appHome;
	
	@Value("${app.datetime.booking-date-format:EEEE, dd 'de' MMMM 'de' yyyy}")
	private String bookingDateFormat;

	/**
	 * Find by organization.
	 *
	 * @param organizationId the organization id
	 * @return the list
	 */
	@Override
	public List<Client> findByOrganization(Long organizationId) {
		securityAccessEntity.controlValidAccessOrganization(organizationId);
		return clientOrganizationRepository.findClientsByOrganization(organizationId);
	}

	/**
	 * Creates the.
	 *
	 * @param client the client
	 * @return the client
	 */
	@Override
	public Client create(Client client) {
	
	    final String email = StringUtils.hasLength(client.getEmail()) ? client.getEmail().trim() : null;
	    final String dni = StringUtils.hasLength(client.getDni()) ? client.getDni().trim() : null;
	    final String phone = StringUtils.hasLength(client.getPhone()) ? client.getPhone().trim() : null;
	    
	    Optional<Client> clientDB = clientRepository.findExistingClientByUniqueFields(email, dni, phone);

	    if (clientDB.isPresent()) {
	        Client existingClient = clientDB.get();
	        String errorMessage = "Client already exists.";
	        
	        if (email != null && email.equals(existingClient.getEmail())) {
	            errorMessage = "Email already exists exception in client";
	        } 
	        else if (dni != null && dni.equals(existingClient.getDni())) {
	            errorMessage = "DNI already exists exception in client";
	        } 
	        else if (phone != null && phone.equals(existingClient.getPhone())) {
	            errorMessage = "Phone already exists exception in client";
	        }
	        throw new ServiceException(ErrorCode.CLIENT_EXISTS, errorMessage);
	    }
	    client.setCreator(SessionUtil.getUserName());
	    client.setCreatedAt(DateUtils.getCurrentDateTime());

		return clientRepository.save(client);
	}

	/**
	 * Delete.
	 *
	 * @param id the id
	 */
	@Override
	public void delete(Long id) {
		if (!clientRepository.existsById(id))
			throw new EntityNotFoundException("Client not found");
		clientRepository.deleteById(id);
	}

	/**
	 * Find all.
	 *
	 * @return the list
	 */
	@Override
	public List<Client> findAll() {
		return clientRepository.findAll();
	}

	/**
	 * Find by id.
	 *
	 * @param id the id
	 * @return the client
	 */
	@Override
	public Client findById(Long id) {
		return clientRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("Client not found with id: " + id));
	}

	/**
	 * Find by email or phone or dni.
	 *
	 * @param email the email
	 * @param phone the phone
	 * @param dni the dni
	 * @return the optional
	 */
	@Override
	@RequireRole(value = {UserRole.ADMIN,UserRole.MANAGER, UserRole.PROVIDER})
	public Optional<Client> findByEmailOrPhoneOrDni(String email, String phone, String dni) {
		return clientRepository
				.findByEmailOrPhoneOrDni(email,phone,dni);
	}

	/**
	 * Count all.
	 *
	 * @return the long
	 */
	@Override
	public long countAll() {
	    return clientRepository.count();
	}
	
	
	/**
	 * Find by provider id.
	 *
	 * @param providerId the provider id
	 * @return the list
	 */
	public List<Client> findByProviderId(Long providerId) {
		// Opcional auditar, lo dejamos sin anotación para reducir ruido.
	    return clientRepository.findByProviderId(providerId);
	}

	/**
	 * Assign client to organization.
	 *
	 * @param clientId the client id
	 * @param organizationId the organization id
	 */
	@Override
	@AuditAspect("CLIENT_ASSIGN_ORG")
	public void assignClientToOrganization(Long clientId, Long organizationId) {
		securityAccessEntity.controlValidAccessOrganization(organizationId);
		Optional<Client> clientDB = clientRepository.findById(clientId);
		if (!clientDB.isPresent()) {
			throw new ServiceException(ErrorCode.CLIENT_NOT_FOUND, "Client not found");
		}
		Optional<Organization> organizationDB = organizationRepository.findById(organizationId);
		if (!organizationDB.isPresent()) {
			throw new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found");
		}	
		
		Optional<ClientOrganization> existing = clientOrganizationRepository.findByClientIdAndOrganizationId(clientId, organizationId);
		if (existing.isPresent()) {
			throw new ServiceException(ErrorCode.CLIENT_NOT_EXISTS_IN_ORGANIZATION, "Client already assigned to organization");
		}

		AuditContext.setOrganization(organizationDB.get());
		AuditContext.get().setObject(clientDB.get().getFullName());

		clientOrganizationRepository.save(ClientOrganization.builder()
				.client(clientDB.get())
				.organization(organizationDB.get())
				.build());
		
	}

	/**
	 * Unassign client from organization.
	 *
	 * @param clientId the client id
	 * @param organizationId the organization id
	 */
	@Override
	@AuditAspect("CLIENT_UNASSIGN_ORG")
	public void unassignClientFromOrganization(Long clientId, Long organizationId) {
		securityAccessEntity.controlValidAccessOrganization(organizationId);
		Optional<Client> clientDB = clientRepository.findById(clientId);
		if (!clientDB.isPresent()) {
			throw new ServiceException(ErrorCode.CLIENT_NOT_FOUND, "Client not found");
		}
		Optional<Organization> organizationDB = organizationRepository.findById(organizationId);
		if (!organizationDB.isPresent()) {
			throw new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found");
		}

		Optional<ClientOrganization> existing = clientOrganizationRepository.findByClientIdAndOrganizationId(clientId, organizationId);
		if (!existing.isPresent()) {
			throw new ServiceException(ErrorCode.CLIENT_NOT_EXISTS_IN_ORGANIZATION, "Client is not assigned to organization");
		}
		
		AuditContext.setOrganization(organizationDB.get());
		AuditContext.get().setObject(clientDB.get().getFullName());

		clientOrganizationRepository.delete(existing.get());
	}

	@Override
	@AuditAspect("CLIENT_NOTIFY")
	public void notifyClient(Long clientId, ClientNotificationDTO dto) {

		Optional<Client> clientOpt = clientRepository.findById(clientId);
		if (!clientOpt.isPresent()) {
			throw new ServiceException(ErrorCode.CLIENT_NOT_FOUND, "Client not found");
		}
		Client client = clientOpt.get();

		Long orgId = dto.getOrganizationId() != null ? dto.getOrganizationId() : com.waturnos.utils.SessionUtil.getOrganizationId();
		if (orgId == null) {
			throw new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not provided");
		}

		securityAccessEntity.controlValidAccessOrganization(orgId);

		Organization organization = organizationRepository.findById(orgId)
				.orElseThrow(() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));

		// Build properties for template
		java.util.Map<String, String> properties = new java.util.HashMap<>();
		properties.put("USERNAME", client.getFullName());
		properties.put("ORG_NAME", organization.getName());
		properties.put("MESSAGE", dto.getMessage());
		properties.put("LINK", appHome != null ? appHome : "");

		String language = dto.getLanguage() != null ? dto.getLanguage() : (organization.getDefaultLanguage() != null ? organization.getDefaultLanguage() : "ES");

		String subject = dto.getSubject();
		if (subject == null || subject.isBlank()) {
			subject = "Mensaje de " + organization.getName();
		}

		NotificationRequest request = NotificationRequest.builder()
				.email(client.getEmail())
				.language(language)
				.subject(subject)
				.type(NotificationType.CLIENT_NOTIFICATION)
				.properties(properties)
				.build();

		AuditContext.setOrganization(organization);
		AuditContext.get().setObject(client.getFullName());
		
		notificationFactory.sendAsync(request);
	}
	
    /**
     * Search clients.
     *
     * @param name the name
     * @param email the email
     * @param phone the phone
     * @param organizationId the organization id
     * @return the list
     */
    public List<Client> searchClients(String name, String email, String phone, String dni, Long organizationId) {
        return clientRepository.search(name, email, phone, dni, organizationId);
    }

	/**
	 * Update.
	 *
	 * @param client the client
	 * @return the client
	 */
	@Override
	public Client update(Client client) {
		Optional<Client> clientDBOptional = clientRepository.findById(client.getId());
		if (!clientDBOptional.isPresent()) {
			throw new ServiceException(ErrorCode.CLIENT_NOT_FOUND, "Client not found");
		}
		Client clientDB = clientDBOptional.get();
		
		// Buscar posibles conflictos en email/dni/phone (puede devolver varios)
		List<Client> conflicts = clientRepository.findExistingClientsByUniqueFields(client.getEmail(),
				client.getDni(), client.getPhone());

		// Excluir al propio cliente que estamos editando
		conflicts.removeIf(c -> c.getId().equals(client.getId()));

		if (!conflicts.isEmpty()) {
			Client existingClient = conflicts.get(0);
			String errorMessage = "Client already exists.";

			if (client.getEmail() != null && client.getEmail().equals(existingClient.getEmail())) {
				errorMessage = "Email already exists exception in client";
			} else if (client.getDni() != null && client.getDni().equals(existingClient.getDni())) {
				errorMessage = "DNI already exists exception in client";
			} else if (client.getPhone() != null && client.getPhone().equals(existingClient.getPhone())) {
				errorMessage = "Phone already exists exception in client";
			}
			throw new ServiceException(ErrorCode.CLIENT_EXISTS, errorMessage);
		}
		clientDB.setFullName(client.getFullName());
		clientDB.setPhone(client.getPhone());
		clientDB.setEmail(client.getEmail());
		clientDB.setDni(client.getDni());
		clientDB.setModificator(SessionUtil.getUserName());
		clientDB.setUpdatedAt(DateUtils.getCurrentDateTime());
		return clientRepository.save(clientDB);
	}

	/**
	 * Check if organization exists.
	 *
	 * @param organizationId the organization id
	 * @return true if exists, false otherwise
	 */
	@Override
	public boolean organizationExists(Long organizationId) {
		return organizationRepository.existsById(organizationId);
	}
	
	/**
	 * Find existing client if exists (does not create, does not throw exception).
	 *
	 * @param organizationId the organization id
	 * @param email the email
	 * @param phone the phone
	 * @return the client or null if not found
	 */
	@Override
	public Client findClientIfExists(Long organizationId, String email, String phone) {
		// Buscar cliente existente por email o phone
		Optional<Client> clientOpt = clientRepository.findByEmailOrPhone(
				StringUtils.hasLength(email) ? email.trim() : null,
				StringUtils.hasLength(phone) ? phone.trim() : null
		);
		
		if (clientOpt.isEmpty()) {
			return null;
		}
		
		Client client = clientOpt.get();
		
		// Verificar que el cliente esté vinculado a la organización
		boolean isLinked = clientOrganizationRepository.existsByClientIdAndOrganizationId(
				client.getId(), organizationId);
		
		// Solo retornar el cliente si está vinculado a la organización
		return isLinked ? client : null;
	}
	
	/**
	 * Register a new client and link to organization.
	 *
	 * @param organizationId the organization id
	 * @param email the email
	 * @param phone the phone
	 * @param fullName the full name
	 * @param dni the document/dni
	 * @return the newly created client
	 * @throws ServiceException if client already exists or validation fails
	 */
	@Override
	public Client registerClient(Long organizationId, String email, String phone, String fullName, String dni) {
		// Validar que al menos uno de los campos esté presente
		if (!StringUtils.hasLength(email) && !StringUtils.hasLength(phone)) {
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Email or phone is required");
		}
		
		// Validar que la organización existe
		Organization organization = organizationRepository.findById(organizationId)
				.orElseThrow(() -> new ServiceException(ErrorCode.ORGANIZATION_NOT_FOUND_EXCEPTION, "Organization not found"));
		
		// Verificar que el cliente NO exista por email, phone o dni
		Optional<Client> existingClient = clientRepository.findByEmailOrPhoneOrDni(
				StringUtils.hasLength(email) ? email.trim() : null,
				StringUtils.hasLength(phone) ? phone.trim() : null,
				StringUtils.hasLength(dni) ? dni.trim() : null
		);
		
		if (existingClient.isPresent()) {
			throw new ServiceException(ErrorCode.CLIENT_EXISTS, "Client already exists");
		}
		
		// Crear nuevo cliente
		Client client = new Client();
		
		if (StringUtils.hasLength(email)) {
			client.setEmail(email.trim());
		}
		
		if (StringUtils.hasLength(phone)) {
			client.setPhone(phone.trim());
		}
		
		if (StringUtils.hasLength(dni)) {
			client.setDni(dni.trim());
		}
		
		// Asignar fullName: usar el proporcionado o generar uno por defecto
		if (StringUtils.hasLength(fullName)) {
			client.setFullName(fullName.trim());
		} else if (StringUtils.hasLength(email)) {
			// Generar nombre por defecto desde el email
			String username = email.trim().contains("@") ? email.trim().split("@")[0] : email.trim();
			client.setFullName(username);
		} else if (StringUtils.hasLength(phone)) {
			// Generar nombre por defecto desde el phone
			client.setFullName("Cliente " + phone.trim());
		}
		
		client.setCreator("SYSTEM");
		client.setCreatedAt(DateUtils.getCurrentDateTime());
		client = clientRepository.save(client);
		
		// Vincular cliente con la organización
		ClientOrganization clientOrg = ClientOrganization.builder()
				.client(client)
				.organization(organization)
				.build();
		clientOrganizationRepository.save(clientOrg);
		
		return client;
	}

	/**
	 * Get upcoming bookings for a client from now onwards.
	 * Optionally filters by organization and date range.
	 *
	 * @param clientId the client id
	 * @param organizationId optional organization id filter
	 * @param fromDate optional start date (defaults to now if null)
	 * @param toDate optional end date (no upper limit if null)
	 * @return the list of client booking DTOs
	 */
	@Override
	public List<ClientBookingDTO> getUpcomingBookings(Long clientId, Long organizationId, 
	                                                   LocalDateTime fromDate, LocalDateTime toDate) {
		// Verificar que el cliente existe
		if (!clientRepository.existsById(clientId)) {
			throw new ServiceException(ErrorCode.CLIENT_NOT_FOUND, "Client not found with id: " + clientId);
		}

		// Si no se especifica fromDate, usar now()
		LocalDateTime effectiveFromDate = fromDate != null ? fromDate : LocalDateTime.now();
		
		List<Booking> bookings = bookingRepository.findUpcomingBookingsByClient(clientId, effectiveFromDate, toDate, organizationId);

		// Formatter para fecha en español desde configuración
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(bookingDateFormat, new Locale("es", "ES"));

		return bookings.stream()
				.map(booking -> {
					String formattedDate = booking.getStartTime().format(formatter);
					// Capitalizar primera letra del día
					formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);

					return ClientBookingDTO.builder()
							.bookingId(booking.getId())
							.formattedDate(formattedDate)
							.startTime(booking.getStartTime())
							.endTime(booking.getEndTime())
							.serviceName(booking.getService().getName())
							.serviceDurationMinutes(booking.getService().getDurationMinutes())
							.providerName(booking.getService().getUser().getFullName())
							.organizationName(booking.getService().getLocation().getOrganization().getName())
							.status(booking.getStatus() != null ? booking.getStatus().name() : null)
							.notes(booking.getNotes())
							.build();
				})
				.collect(Collectors.toList());
	}

}
