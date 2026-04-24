package com.cv.security.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cv.security.config.KeycloakProperties;
import com.cv.security.exception.AuthException;
import com.cv.security.dto.PersonalDetailsDto;
import com.cv.security.keycloak.dto.KeycloakUserDto;

@Service
public class UserService {

	@Autowired
	private AuthService authService;

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired
	private KeycloakService keycloakService;

	@Autowired
	private ProfileService profileService;

	@Autowired
	private KeycloakProperties keycloakProperties;

	/**
	 * Checks if an email already exists in the system
	 * 
	 * @param email The email to check
	 * @return true if the email exists, false otherwise
	 */
	public boolean checkIfEmailExists(String email) {
		// Check for null or empty email
		if (email == null || email.trim().isEmpty()) {
			return false;
		}

		boolean existsInKeycloak = false;

		try {
			// Get users by email from Keycloak
			List<KeycloakUserDto> existingUsers = keycloakService.getUsersByEmail(keycloakProperties.getRealm(), email,
					"Bearer " + authService.getAdminAccessToken());
			existsInKeycloak = (existingUsers != null && !existingUsers.isEmpty());
		} catch (Exception ex) {
			logger.warn("Error checking user in Keycloak: {}", ex.getMessage());
		}

		return existsInKeycloak;
	}

	public PersonalDetailsDto fetchUserInfoFromProfileApi(String userId) throws AuthException {
		try {
			return profileService.getUserById(userId);
		} catch (Exception e) {
			logger.error("Failed to retrieve user from profile service", e);
			throw new AuthException("Failed to retrieve user from profile service", e);
		}
	}

}
