package com.cv.security.exception;

/**
 * Exception thrown for authentication related errors
 */
public class AuthException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public AuthException(String message) {
		super(message);
	}

	public AuthException(String message, Throwable cause) {
		super(message, cause);
	}
}
