package com.cv.security.controller;

import java.security.Principal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cv.security.entity.User;
import com.cv.security.exception.AuthException;
import com.cv.security.service.AuthService;
import com.cv.security.dto.PasswordChangeRequest;
import com.cv.security.dto.PersonalDetailsDto;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

	@Autowired
	private AuthService authService;

	/**
	 * Authenticate a user with username and password
	 * 
	 * @param credentials Map containing username and password
	 * @return Authentication token response
	 * @throws AuthException
	 */
	@PostMapping("/login")
	public ResponseEntity<?> authenticate(@RequestBody Map<String, String> credentials) throws AuthException {
		logger.debug("Authentication request received");

		String username = credentials.get("username");
		if (username == null || username.trim().isEmpty()) {
			logger.warn("Authentication attempt with empty username");
			return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
		}

		String password = credentials.get("password");
		if (password == null || password.trim().isEmpty()) {
			logger.warn("Authentication attempt with empty password");
			return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
		}

		Map<String, Object> response = authService.authenticate(username, password);
		return ResponseEntity.ok(response);
	}

	/**
	 * Refresh an authentication token
	 * 
	 * @param refreshToken The refresh token
	 * @return New authentication token response
	 * @throws AuthException
	 */
	@PostMapping("/refresh-token")
	public ResponseEntity<?> refreshToken(@RequestHeader("refresh_token") String refreshToken) throws AuthException {
		logger.debug("Token refresh request received");

		if (refreshToken == null || refreshToken.trim().isEmpty()) {
			logger.warn("Token refresh attempt with empty refresh token");
			return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
		}

		Map<String, Object> response = authService.refreshToken(refreshToken);
		return ResponseEntity.ok(response);
	}

	/**
	 * Update user details
	 * 
	 * @param updates Map of user attributes to update
	 * @param jwt     JWT token of the authenticated user
	 * @return Update status
	 */
	@PutMapping("/update-user")
	public ResponseEntity<?> updateUserDetails(@RequestBody Map<String, Object> updates,
			@AuthenticationPrincipal Jwt jwt) {
		logger.debug("User update request received");

		if (updates == null || updates.isEmpty()) {
			logger.warn("User update attempt with empty updates");
			return ResponseEntity.badRequest().body(Map.of("error", "No updates provided"));
		}

		String username = jwt.getClaimAsString("preferred_username");
		logger.debug("Updating user: {}", username);

		String accessToken = authService.getAdminAccessToken();
		String userId = authService.getUserIdByUsername(username, accessToken);

		if (userId == null) {
			logger.warn("User not found for username: {}", username);
			return ResponseEntity.notFound().build();
		}

		boolean success = authService.updateUser(userId, updates, accessToken);

		logger.debug("User update completed with success: {}", success);
		return ResponseEntity.ok(Map.of("status", "success"));
	}

	@PostMapping("/register-user")
	public ResponseEntity<?> registerUser(@RequestBody PersonalDetailsDto request, Principal principal) {
		logger.debug("Registering user via /admin/register");

		if (request == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "No user details provided"));
		}

		try {
			User user = authService.registerUser(request, principal);
			// Recommend emailing password instead of returning in response
			logger.info("Registered user details: {}", user.getRoles());
			return ResponseEntity.ok(user);
		} catch (Exception ex) {
			logger.error("Error while registering user: {}", ex.getMessage(), ex);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "Registration failed", "details", ex.getMessage()));
		}
	}

	@PostMapping("/update-password")
	public ResponseEntity<?> updatePassword(@RequestBody PasswordChangeRequest request, Principal principal) {
		if (principal == null || request.getUserId() == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("error", "Unauthorized or missing user ID"));
		}

		if (!(principal instanceof JwtAuthenticationToken jwtAuth)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid principal type"));
		}

		String keycloakUserId = jwtAuth.getToken().getSubject(); // Keycloak UUID (sub claim)
		if (!keycloakUserId.equals(request.getUserId())) {
			logger.warn("User ID mismatch: authenticated [{}] vs provided [{}]", keycloakUserId, request.getUserId());
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "You are not allowed to update this user's password."));
		}

		String email = (String) jwtAuth.getTokenAttributes().get("email");
		String username = (String) jwtAuth.getTokenAttributes().get("preferred_username");

		try {
			return authService.updatePassword(email, username, request);
		} catch (Exception e) {
			logger.error("Unexpected error", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}

	}

}