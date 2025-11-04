package com.waturnos.controller.stateless;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.waturnos.dto.request.ForgotPassword;
import com.waturnos.dto.request.ResetPassword;
import com.waturnos.service.AuthService;

@RestController
@RequestMapping("/public/users") 
public class UserControllerStateless {

    private final AuthService authService;

    public UserControllerStateless(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Forgot password.
     *
     * @param email the email
     * @param userType the user type
     * @return the response entity
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword( @RequestBody(required = true) ForgotPassword forgotPassword) {
        try {
			authService.createPasswordResetTokenAndSendEmail(forgotPassword.getEmail(),
					forgotPassword.getUserType().name());
            return ResponseEntity.ok("Se ha enviado un correo electrónico con instrucciones para restablecer tu clave.");
        } catch (RuntimeException e) {
            // Es buena práctica de seguridad devolver un mensaje genérico
            // aunque el email no exista, para no dar pistas a atacantes.
        	 return ResponseEntity.ok("Se ha enviado el correo con instrucciones para restablecer tu clave.");
        }
    }

    /**
     * Reset password.
     *
     * @param token the token
     * @param newPassword the new password
     * @param userType the user type
     * @return the response entity
     */
    // 2. Endpoint para restablecer la clave (llamado desde el formulario)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
        @RequestBody(required = true) ResetPassword resetPassword) 
    {
        try {
			authService.resetPassword(resetPassword.getToken(), resetPassword.getNewPassword(),
					resetPassword.getUserType().name());
            return ResponseEntity.ok("Tu clave ha sido restablecida con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
