package com.waturnos.entity;

import com.waturnos.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="user")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    private UserRole role;
    private Boolean active = true;
    private LocalDateTime lastLoginAt;
    private String creator;
    private String modificator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="organization_id")
    private Organization organization;
}
