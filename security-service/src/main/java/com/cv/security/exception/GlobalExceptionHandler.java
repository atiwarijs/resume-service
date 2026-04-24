package com.cv.security.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("error", "Bad Request");
		body.put("message", ex.getMessage());
		body.put("path", request.getDescription(false));

		logger.error("Invalid argument: {}", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.put("error", "Internal Server Error");
		body.put("message", "An unexpected error occurred");
		body.put("path", request.getDescription(false));

		logger.error("Unhandled exception: ", ex);
		return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.FORBIDDEN.value());
		body.put("error", "Forbidden");
		body.put("message", "You don't have permission to access this resource");
		body.put("path", request.getDescription(false));

		logger.error("Access denied: {}", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(HttpClientErrorException.class)
	public ResponseEntity<Object> handleHttpClientErrorException(HttpClientErrorException ex, WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", ex.getStatusCode().value());
		body.put("error", ex.getStatusText());
		body.put("message", ex.getResponseBodyAsString());
		body.put("path", request.getDescription(false));

		logger.error("HTTP client error: {} - {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
		return new ResponseEntity<>(body, ex.getStatusCode());
	}

	@ExceptionHandler(RestClientException.class)
	public ResponseEntity<Object> handleRestClientException(RestClientException ex, WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
		body.put("error", "Service Unavailable");
		body.put("message", "External service communication error");
		body.put("path", request.getDescription(false));

		logger.error("REST client error: {}", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.NOT_FOUND.value());
		body.put("error", "Not Found");
		body.put("message", ex.getMessage());
		body.put("path", request.getDescription(false));

		logger.error("Resource not found: {}", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(AuthException.class)
	public ResponseEntity<Object> handleAuthException(AuthException ex, WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.UNAUTHORIZED.value());
		body.put("error", "Unauthorized");
		body.put("message", ex.getMessage());
		body.put("path", request.getDescription(false));

		logger.error("Authentication error: {}", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(KeycloakIntegrationException.class)
	public ResponseEntity<Object> handleKeycloakIntegrationException(KeycloakIntegrationException ex,
			WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
		body.put("error", "Service Unavailable");
		body.put("message", ex.getMessage());
		body.put("path", request.getDescription(false));

		logger.error("Keycloak integration error: {}", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, WebRequest request) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now().toString());
		body.put("status", HttpStatus.UNAUTHORIZED.value());
		body.put("error", "Unauthorized");
		body.put("message", "Invalid credentials");
		body.put("path", request.getDescription(false));

		logger.error("Bad credentials: {}", ex.getMessage());
		return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
	}
	
	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Object> handleEmailExists(EmailAlreadyExistsException ex, WebRequest request) {
	    Map<String, Object> body = new LinkedHashMap<>();
	    body.put("timestamp", LocalDateTime.now().toString());
	    body.put("status", HttpStatus.CONFLICT.value());
	    body.put("error", "Conflict");
	    body.put("message", ex.getMessage());
	    body.put("path", request.getDescription(false));
	    
	    logger.error("Email already exists: {}", ex.getMessage());
	    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
	}
}