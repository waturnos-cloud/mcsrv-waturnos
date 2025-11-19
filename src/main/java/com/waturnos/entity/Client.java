package com.waturnos.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"clientOrganizations"})
public class Client implements CommonUser {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String fullName;
	private String email;
	private String phone;
	private String password;
	private String dni;
	private String creator;
	private String modificator;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

    @OneToMany(
        mappedBy = "client", 
        cascade = CascadeType.ALL, 
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    @Builder.Default
    private Set<ClientOrganization> clientOrganizations = new HashSet<>();
	
	public static final String CLIENT = "CLIENT";
	
	@Override
    public String getUserType() {
        return CLIENT;
    }
}
