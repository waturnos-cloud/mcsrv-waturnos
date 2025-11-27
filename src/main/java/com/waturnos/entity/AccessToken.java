package com.waturnos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "access_token")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 6, nullable = false)
    private String code;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
