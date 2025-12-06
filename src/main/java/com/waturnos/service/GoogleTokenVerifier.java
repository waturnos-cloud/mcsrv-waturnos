package com.waturnos.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para verificar tokens de Google OAuth.
 */
@Service
@Slf4j
public class GoogleTokenVerifier {
	
	@Value("${google.client.id}")
	private String clientId;
	
	/**
	 * Verifica un token ID de Google y extrae la información del usuario.
	 *
	 * @param idTokenString el token ID de Google
	 * @return el payload con la información del usuario
	 * @throws GeneralSecurityException si hay un error de seguridad
	 * @throws IOException si hay un error de I/O
	 * @throws IllegalArgumentException si el token es inválido
	 */
	public Payload verifyToken(String idTokenString) throws GeneralSecurityException, IOException {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
				new NetHttpTransport(), 
				GsonFactory.getDefaultInstance())
			.setAudience(Collections.singletonList(clientId))
			.build();
		
		GoogleIdToken idToken = verifier.verify(idTokenString);
		if (idToken != null) {
			Payload payload = idToken.getPayload();
			log.info("Token verificado exitosamente para email: {}", payload.getEmail());
			return payload;
		} else {
			log.warn("Token ID de Google inválido");
			throw new IllegalArgumentException("Token de Google inválido");
		}
	}
}
