package com.waturnos.repository;

import com.waturnos.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByEmailAndCode(String email, String code);
    Optional<AccessToken> findByPhoneAndCode(String phone, String code);

    @Modifying
    @Query("DELETE FROM AccessToken t WHERE t.expiryDate < :now")
    void deleteExpired(@Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM AccessToken t WHERE t.email = :email OR t.phone = :phone")
    void deleteByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);
}
