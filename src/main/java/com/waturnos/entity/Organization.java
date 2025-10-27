package com.waturnos.entity;

import com.waturnos.enums.OrganizationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "organization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
