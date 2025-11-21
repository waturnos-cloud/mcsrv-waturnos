package com.waturnos.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.waturnos.entity.Client;
import com.waturnos.entity.CommonUser;
import com.waturnos.entity.PasswordResetToken;
import com.waturnos.entity.User;
import com.waturnos.notification.bean.NotificationRequest;
import com.waturnos.notification.enums.NotificationType;
import com.waturnos.notification.factory.NotificationFactory;
import com.waturnos.repository.ClientRepository;
import com.waturnos.repository.PasswordResetTokenRepository;
import com.waturnos.repository.UserRepository;
import com.waturnos.service.AuthService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

import lombok.RequiredArgsConstructor;

/**
 * The Class BookingServiceImpl.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationFactory notificationFactory; 
    private final MessageSource messageSource;

    @Value("${app.notification.RESET_PASSWORD}")
    private String baseUrlResetPassword; 
    
    @Value("${app.resetpassword.expiration.timetoken:10}")
    private Long resetPasswordMinutes;
    

    /**
     * Creates the password reset token and send email.
     *
     * @param email the email
     * @param userType the user type
     */
    @Transactional
    public void createPasswordResetTokenAndSendEmail(String email, String userType) {
    	CommonUser user = null;
    	if(User.USER.equals(userType)){
    		user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND, "Usuario no encontrado"));
    	}else {
    		user = clientRepository.findByEmail(email)
                    .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND, "Usuario no encontrado"));
    	}	
        
        String token = UUID.randomUUID().toString();
        
        if(User.USER.equals(user.getUserType())) {
        	tokenRepository.deleteByUserId(user.getId());
        }else {
        	tokenRepository.deleteByClientId(user.getId());
        }	

        // 3. Guarda el nuevo token temporal
		PasswordResetToken resetToken = PasswordResetToken.builder().token(token).expiryDate(LocalDateTime.now().plusMinutes(resetPasswordMinutes)).build();
		if(User.USER.equals(user.getUserType())) {
			resetToken.setUser((User)user);
		}else {
			resetToken.setClient((Client)user);
		}
        
        tokenRepository.save(resetToken);

        // 4. Envía el email con el enlace
        notificationFactory.sendAsync(buildRequestRestPassword(user, resetToken));
    }
	

	/**
	 * Builds the request.
	 *
	 * @param user the manager
	 * @param temporalPasswordUser 
	 * @return the notification request
	 */
	private NotificationRequest buildRequestRestPassword(CommonUser user, PasswordResetToken resetToken) {
		Map<String, String> properties = new HashMap<>();
		properties.put("USERNAME", user.getFullName());
		properties.put("EXPIRATION_MINUTES", String.valueOf(resetPasswordMinutes));
		properties.put("LINK",baseUrlResetPassword+"?token="+resetToken.getToken()+"&userType="+user.getUserType()	);
		return NotificationRequest.builder().email(user.getEmail()).language("ES")
				.subject(messageSource.getMessage("notification.subject.resetPassword", null,
						LocaleContextHolder.getLocale()))
				.type(NotificationType.RESET_PASSWORD).properties(properties).build();
	}

    @Transactional
    public void resetPassword(String token, String newPassword, String userType) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido o expirado."));

        // 1. Valida la expiración del token
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken); // Elimina el token expirado
            throw new ServiceException(ErrorCode.PASSWORD_RESET_TOKEN_EXPIRED,"El token ha expirado. Por favor, solicita un nuevo enlace.");
        }

        if(User.USER.equals(userType)) {
	        User user = resetToken.getUser();
	        user.setPassword(passwordEncoder.encode(newPassword));
	        userRepository.save(user);
        }else {
	        Client client = resetToken.getClient();
	        client.setPassword(passwordEncoder.encode(newPassword));
	        clientRepository.save(client);
        }    
        tokenRepository.delete(resetToken);
    }
}