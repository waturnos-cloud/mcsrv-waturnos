package com.waturnos.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.waturnos.enums.UserRole;

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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"organization"})
public class User implements CommonUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private String password;
	private String photoUrl;
	private String bio;
	@Enumerated(EnumType.STRING)
	private UserRole role;
	@Builder.Default
	private Boolean active = true;
	private LocalDateTime lastLoginAt;
	private String creator;
	private String modificator;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id")
	private Organization organization;
	
	@OneToMany(mappedBy = "service")
	private List<ServiceEntity> services;
	
	@Transient 
    private Long idOrganization;

	/** The Constant USER. */
	public static final String USER = "USER";
	
	@Override
	public String getUserType() {
		return USER;
	}
	
}
