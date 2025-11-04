package com.waturnos.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "password_reset_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class PasswordResetToken {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "user_id")
    private User user;
    
    @OneToOne(targetEntity = User.class, fetch = FetchType.LAZY)
    @JoinColumn(nullable = true, name = "client_id")
    private Client client;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
    
    /**
     * Calculate expiry date.
     *
     * @param minutes the minutes
     * @return the local date time
     */
    public LocalDateTime calculateExpiryDate(long minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }
    

}
    
