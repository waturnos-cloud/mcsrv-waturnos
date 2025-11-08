package com.waturnos.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;

public interface GoogleAuthService {
	
	public GoogleIdToken.Payload verifyToken(String idTokenString);

}
