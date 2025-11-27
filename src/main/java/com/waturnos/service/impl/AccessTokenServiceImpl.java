package com.waturnos.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.AccessToken;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.AccessTokenRepository;
import com.waturnos.service.AccessTokenService;

import lombok.RequiredArgsConstructor;

/**
 * The Class AccessTokenServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class AccessTokenServiceImpl implements AccessTokenService {
    
    /** The repository. */
    private final AccessTokenRepository repository;
    private final NotificationFactory notificationFactory;
    
    private final MessageSource messageSource;
    
    /** The Constant EXPIRY_MINUTES. */
    private static final int EXPIRY_MINUTES = 5;

    /**
     * Generate token.
     *
     * @param email the email
     * @param phone the phone
     */
    @Override
    @Transactional
    public void generateToken(String email, String phone) {
        // Opcional: borrar tokens previos para ese email/phone
        repository.deleteByEmailOrPhone(email, phone);
        String code = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(EXPIRY_MINUTES);
        AccessToken token = AccessToken.builder()
                .email(email)
                .phone(phone)
                .code(code)
                .expiryDate(expiry)
                .build();
        repository.save(token);
        
        notificationFactory.sendAsync(buildRequest(token));
    }
    
	/**
	 * Builds the request.
	 *
	 * @param manager the manager
	 * @param temporalPasswordUser 
	 * @return the notification request
	 */
	private NotificationRequest buildRequest(AccessToken token) {
		Map<String, String> properties = new HashMap<>();
        properties.put("ACCESS_CODE", token.getCode());
        properties.put("USERNAME", token.getEmail());
        properties.put("EXPIRATION_MINUTES",  String.valueOf(EXPIRY_MINUTES));
		return NotificationRequest
				.builder().email(token.getEmail()).phone(token.getPhone()).language("ES")
				.subject(messageSource
				.getMessage("notification.subject.access.code", null, LocaleContextHolder.getLocale()))
				.type(NotificationType.ACCESS_TOKEN)
				.properties(properties).build();
	}

    /**
     * Validate token.
     *
     * @param email the email
     * @param phone the phone
     * @param code the code
     * @return the optional
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AccessToken> validateToken(String email, String phone, String code) {
        Optional<AccessToken> token = (email != null)
                ? repository.findByEmailAndCode(email, code)
                : repository.findByPhoneAndCode(phone, code);
        if (token.isPresent() && token.get().getExpiryDate().isAfter(LocalDateTime.now())) {
            return token;
        }
        return Optional.empty();
    }

    /**
     * Delete token.
     *
     * @param id the id
     */
    @Override
    @Transactional
    public void deleteToken(Long id) {
        repository.deleteById(id);
    }

    /**
     * Delete expired tokens.
     */
    @Override
    @Transactional
    public void deleteExpiredTokens() {
        repository.deleteExpired(LocalDateTime.now());
    }
}
