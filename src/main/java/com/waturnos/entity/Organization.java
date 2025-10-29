package com.waturnos.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.waturnos.enums.OrganizationStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"locations","providers"})
public class Organization {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	private String logoUrl;
	private String timezone;
	private String type;
	private String defaultLanguage;
	@Builder.Default
	private Boolean active = true;
	@Enumerated(EnumType.STRING)
	private OrganizationStatus status;
	private String creator;
	private String modificator;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "organization")
	private List<Location> locations;
	
	@ManyToMany(mappedBy = "organizations")
    private List<Provider> providers;

}
