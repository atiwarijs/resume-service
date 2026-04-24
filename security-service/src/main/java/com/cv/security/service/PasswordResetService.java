package com.cv.security.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cv.security.config.KeycloakProperties;
import com.cv.security.dto.PasswordResetRequest;
import com.cv.security.dto.PasswordResetResponse;
import com.cv.security.entity.User;
import com.cv.security.repository.UserRepository;
import com.cv.security.keycloak.dto.KeycloakUserDto;

@Service
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    
    @Autowired
    private KeycloakProperties keycloakProperties;
    
    @Autowired
    private KeycloakService keycloakService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${app.password-reset.token-expiry-minutes:15}")
    private int tokenExpiryMinutes;
    
    // In a real application, you'd store this in a database with expiry
    private Map<String, String> resetTokens = new HashMap<>();
    
    /**
     * Generate and send password reset link to the provided email
     * 
     * @param request Password reset request containing email
     * @return PasswordResetResponse with status
     */
    public PasswordResetResponse sendPasswordResetLink(PasswordResetRequest request) {
        try {
            String email = request.getEmail();
            logger.debug("Processing password reset request for email: {}", email);
            
            // Get admin access token
            String adminToken = getAdminAccessToken();
            
            // Find user by email in Keycloak
            List<KeycloakUserDto> users = keycloakService.getUsersByEmail(keycloakProperties.getRealm(), 
                    email, "Bearer " + adminToken);
            
            if (users == null || users.isEmpty()) {
                logger.warn("No user found with email: {}", email);
                // For security reasons, don't reveal that the email doesn't exist
                return PasswordResetResponse.success("If your email is registered, you will receive a password reset link shortly.");
            }
            
            KeycloakUserDto user = users.get(0);
            String userId = user.getId();
            
            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            resetTokens.put(resetToken, userId);
            
            // Send reset email
            emailService.sendPasswordResetEmail(email, resetToken);
            
            logger.info("Password reset link sent to email: {}", email);
            return PasswordResetResponse.success("Password reset link has been sent to your email.");
            
        } catch (Exception e) {
            logger.error("Error communicating with Keycloak during password reset", e);
            return PasswordResetResponse.failure("Password reset service is currently unavailable. Please try again later.");
        }
    }
    
    /**
     * Reset password using the provided token
     * 
     * @param token Reset token
     * @param newPassword New password
     * @return PasswordResetResponse with status
     */
    public PasswordResetResponse resetPassword(String token, String newPassword) {
        try {
            logger.debug("Processing password reset with token: {}", token);
            
            // Validate token
            String userId = resetTokens.get(token);
            if (userId == null) {
                logger.warn("Invalid reset token used: {}", token);
                return PasswordResetResponse.failure("Invalid or expired reset token.");
            }
            
            // Get admin access token
            String adminToken = getAdminAccessToken();
            
            // Get user details
            KeycloakUserDto user = keycloakService.getUserById(keycloakProperties.getRealm(), 
                    userId, "Bearer " + adminToken);
            
            if (user == null) {
                logger.warn("User not found for reset token: {}", token);
                return PasswordResetResponse.failure("User not found.");
            }
            
            // Reset password in Keycloak
            Map<String, Object> passwordPayload = new HashMap<>();
            passwordPayload.put("type", "password");
            passwordPayload.put("value", newPassword);
            passwordPayload.put("temporary", false);
            
            keycloakService.resetPassword(keycloakProperties.getRealm(), userId, passwordPayload,
                    "Bearer " + adminToken);
            
            // Update local database if user exists
            User localUser = userRepository.findById(userId).orElse(null);
            if (localUser != null) {
                localUser.setUpdatePassword(true);
                userRepository.save(localUser);
            }
            
            // Remove the used token
            resetTokens.remove(token);
            
            logger.info("Password reset successfully for user: {}", user.getEmail());
            return PasswordResetResponse.success("Password has been reset successfully.");
            
        } catch (Exception e) {
            logger.error("Error communicating with Keycloak during password reset", e);
            return PasswordResetResponse.failure("Password reset service is currently unavailable. Please try again later.");
        }
    }
    
    /**
     * Get admin access token for Keycloak operations
     */
    private String getAdminAccessToken() {
        try {
            logger.debug("Getting admin access token for password reset");
            
            var tokenRequest = com.cv.security.keycloak.dto.TokenRequestDto.forClientCredentials(
                    keycloakProperties.getAdminClientId(),
                    keycloakProperties.getAdminClientSecret());
            
            var tokenResponse = keycloakService.getToken(keycloakProperties.getAdminRealm(), tokenRequest);
            
            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                throw new RuntimeException("Admin token response is invalid");
            }
            
            return tokenResponse.getAccessToken();
        } catch (Exception e) {
            logger.error("Failed to get admin access token", e);
            throw new RuntimeException("Invalid admin client credentials", e);
        }
    }
}

