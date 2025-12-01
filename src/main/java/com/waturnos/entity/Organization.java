package com.waturnos.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.waturnos.enums.OrganizationStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"locations","users","clientOrganizations"})
public class Organization {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String logoUrl;
	private String subdomain;
	private String timezone;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type")
	private Category type;
	private String defaultLanguage;
	@Builder.Default
	private Boolean active = true;
	@Enumerated(EnumType.STRING)
	private OrganizationStatus status;
	private String creator;
	private String modificator;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private boolean simpleOrganization;

	@OneToMany(mappedBy = "organization")
	private List<Location> locations;
	
	@OneToMany(mappedBy = "organization")
    private List<User> users;
	
	@OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ClientOrganization> clientOrganizations;

}
