package com.waturnos.service.impl;

import java.security.GeneralSecurityException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.waturnos.service.GoogleAuthService;
import com.waturnos.service.exceptions.ErrorCode;
import com.waturnos.service.exceptions.ServiceException;

import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * The Class GoogleAuthServiceImpl.
 */
@Service
@Slf4j
public class GoogleAuthServiceImpl implements GoogleAuthService {

	private final NetHttpTransport transport = new NetHttpTransport();
	private final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

	@Value("${google.client_id:NOT_DEFINED}")
	private String googleClientId;

	/**
	 * Verify token.
	 *
	 * @param idTokenString the id token string
	 * @return the google id token. payload
	 */
	public GoogleIdToken.Payload verifyToken(String idTokenString) {
		try {
			GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
					.setAudience(Collections.singletonList(googleClientId)).build();

			GoogleIdToken idToken = null;
			try {
				idToken = verifier.verify(idTokenString);
			} catch (java.io.IOException e) {
				log.error("Error verifiyng google token", e);
			}
			if (idToken != null) {
				return idToken.getPayload();
			} else {
				throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Token inválido");
			}
		} catch (GeneralSecurityException e) {
			log.error(e.getMessage(),e);
			throw new ServiceException(ErrorCode.GLOBAL_ERROR,
					"Error de seguridad al verificar el token: " + e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage(),e);
			throw new ServiceException(ErrorCode.GLOBAL_ERROR, "Error de red al verificar el token: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			log.error(e.getMessage(),e);
			throw new ServiceException(ErrorCode.GLOBAL_ERROR,
					"Argumento inválido al verificar token: " + e.getMessage());
		}
	}
}