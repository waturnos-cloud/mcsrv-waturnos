package com.waturnos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="provider")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Provider {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String photoUrl;
    private String bio;
    private Boolean active = true;
    private String creator;
    private String modificator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="organization_id")
    private Organization organization;
}
