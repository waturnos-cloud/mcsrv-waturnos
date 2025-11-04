package com.waturnos.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.waturnos.entity.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
	
	// Método para buscar un token específico
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUserId(Long userId);
    
    void deleteByClientId(Long clientId);
	
}
