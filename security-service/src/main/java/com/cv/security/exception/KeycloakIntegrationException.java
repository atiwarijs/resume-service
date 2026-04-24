package com.cv.security.exception;

public class KeycloakIntegrationException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public KeycloakIntegrationException(String message) {
		super(message);
	}

	public KeycloakIntegrationException(String message, Throwable cause) {
		super(message, cause);
	}
}