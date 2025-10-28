package com.waturnos.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provider")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Provider {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private String photoUrl;
	private String bio;
	@Builder.Default
	private Boolean active = true;
	private String creator;
	private String modificator;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "provider_organization", 
        joinColumns = @JoinColumn(name = "provider_id"), 
        inverseJoinColumns = @JoinColumn(name = "organization_id")
    )
    private List<Organization> organizations;
}
