package com.cv.security.service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import com.cv.security.config.KeycloakProperties;
import com.cv.security.entity.User;
import com.cv.security.entity.UserRole;
import com.cv.security.event.UserEventPublisher;
import com.cv.security.exception.AuthException;
import com.cv.security.exception.EmailAlreadyExistsException;
import com.cv.security.exception.KeycloakIntegrationException;
import com.cv.security.exception.ResourceNotFoundException;
import com.cv.security.repository.UserRepository;
import com.cv.security.dto.PasswordChangeRequest;
import com.cv.security.dto.PersonalDetailsDto;
import com.cv.security.keycloak.dto.KeycloakRoleDto;
import com.cv.security.keycloak.dto.KeycloakTokenResponse;
import com.cv.security.keycloak.dto.KeycloakUserDto;
import com.cv.security.keycloak.dto.TokenRequestDto;
import com.cv.security.keycloak.dto.UserRegistrationDto;

import jakarta.transaction.Transactional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private KeycloakProperties keycloakProperties;

    @Autowired
    private KeycloakService keycloakService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserEventPublisher userEventPublisher;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Find an existing user by ID or create a new one
     *
     * @param userId   User ID from Keycloak
     * @param username Username
     * @param email    Email address
     * @return User object
     * @throws AuthException
     */
    public User findAndUpdateUser(String userId, String username, String email) throws AuthException {
        try {
            logger.debug("Looking up user with ID: {}", userId);

            Optional<User> existingUser = userRepository.findById(userId);
            if (existingUser.isPresent()) {
                logger.debug("Found existing user: {}", username);
                User user = existingUser.get();

                boolean updated = false;
                if (username != null && !username.equals(user.getUsername())) {
                    user.setUsername(username);
                    updated = true;
                }
                if (email != null && !email.equals(user.getEmail())) {
                    user.setEmail(email);
                    updated = true;
                }

                if (updated) {
                    logger.debug("Updating existing user with new information");
                    User updatedUser = userRepository.save(user);
//					userEventPublisher.publishUserUpdated(PersonalDetailsMapper.toDto(updatedUser));
                    return updatedUser;
                }

                return user;
            } else {
                logger.debug("Creating new user: {}", username);
                User newUser = new User();
                newUser.setId(userId);
                newUser.setUsername(username);
                newUser.setEmail(email);
                User savedUser = userRepository.save(newUser);
//				userEventPublisher.publishUserCreated(UserMapper.toDto(savedUser));
                return savedUser;
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid UUID format: {}", userId, e);
            throw new IllegalArgumentException("Invalid user ID format", e);
        } catch (Exception e) {
            logger.error("Error while finding or creating user", e);
            throw new AuthException("Failed to process user data", e);
        }
    }

    /**
     * Create a new user if not exists
     *
     * @param userInfo User information map
     * @return User object
     * @throws AuthException
     */
    public User createNonExistingUser(Map<String, Object> userInfo) throws AuthException {
        try {
            String userId = (String) userInfo.get("id");
            String email = (String) userInfo.get("email");
            String firstName = (String) userInfo.get("firstName");
            String lastName = (String) userInfo.get("lastName");
            String username = (String) userInfo.get("username");

            logger.debug("Looking up user with ID: {}", userId);
            Optional<User> existingUser = userRepository.findById(userId);
            if (existingUser.isPresent()) {
                logger.debug("User already exists: {}", username);
                return existingUser.get();
            } else {
                logger.debug("Creating new user: {}", username);
                User newUser = new User();
                newUser.setId(userId);
                newUser.setUsername(username);
                newUser.setEmail(email);
                newUser.setFirstName(firstName);
                newUser.setLastName(lastName);
                newUser.setCreatedBy(email);
                User savedUser = userRepository.save(newUser);
//				userEventPublisher.publishUserCreated(UserMapper.toDto(savedUser));
                return savedUser;
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user ID format", e);
        } catch (Exception e) {
            logger.error("Error while finding or creating user", e);
            throw new AuthException("Failed to process user data", e);
        }
    }

    /**
     * Get user by ID
     *
     * @param userId User ID
     * @return User object
     * @throws ResourceNotFoundException if user not found
     */
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    /**
     * Authenticates a user against Keycloak and returns token information. Also
     * checks if password update is required.
     *
     * @param username The username to authenticate
     * @param password The password to authenticate with
     * @return A map containing authentication token and additional user information
     * @throws AuthException If authentication fails
     */
    public Map<String, Object> authenticate(String username, String password) throws AuthException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthException("Password cannot be empty");
        }

        try {
            logger.debug("Authenticating user: {}", username);

            // Call keycloakService with individual parameters instead of TokenRequestDto
            TokenRequestDto tokenRequest = TokenRequestDto.forPasswordGrant(keycloakProperties.getClientId(),
                    keycloakProperties.getClientSecret(), username, password);
            KeycloakTokenResponse tokenResponse = keycloakService.getToken(keycloakProperties.getRealm(), tokenRequest);

            // Create a result map that will contain both token data and additional info
            Map<String, Object> result = new HashMap<>();

            // Copy all token response data to the result
            if (tokenResponse != null) {
                result.put("access_token", tokenResponse.getAccessToken());
                result.put("refresh_token", tokenResponse.getRefreshToken());
                result.put("expires_in", tokenResponse.getExpiresIn());
            }

            // Extract user info and sync with our database
            if (tokenResponse != null && tokenResponse.getAccessToken() != null) {
                String accessToken = tokenResponse.getAccessToken();
                // Get user details from token or from Keycloak if needed
                String adminToken = getAdminAccessToken();
                String userId = getUserIdByUsernameOrEmail(username, adminToken);

                // Check if user needs to update password
                // Check if user needs to update password
                if (userId != null) {
                    Optional<User> existingUser = userRepository.findById(userId);
                    if (existingUser.isPresent() && !existingUser.get().isUpdatePassword()) {
                        // Add password update flag to result but keep token information
                        result.put("status", "PASSWORD_UPDATE_REQUIRED");
                        result.put("passwordUpdateRequired", true);
                    }
                }

                if (userId != null) {
                    KeycloakUserDto userDto = keycloakService.getUserById(keycloakProperties.getRealm(), userId,
                            "Bearer " + adminToken);

                    if (userDto != null) {
                        // Convert KeycloakUserDto to Map for consistency with existing code
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("id", userDto.getId());
                        userInfo.put("username", userDto.getUsername());
                        userInfo.put("email", userDto.getEmail());
                        userInfo.put("firstName", userDto.getFirstName());
                        userInfo.put("lastName", userDto.getLastName());

                        // Create or update user in our database
                        createNonExistingUser(userInfo);

                        // Add user info to the result
                        result.put("userId", userId);
                        result.put("userInfo", userInfo);
                    }
                }
            }

            return result;
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", username, e);
            throw new AuthException("Invalid credentials", e);

        }
    }

    /**
     * Get user ID by username or email from Keycloak
     *
     * @param usernameOrEmail the username or email to search for
     * @param accessToken     admin access token
     * @return User ID or null if not found
     * @throws KeycloakIntegrationException if Keycloak API call fails
     */
    public String getUserIdByUsernameOrEmail(String usernameOrEmail, String accessToken) {
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Username or email cannot be empty");
        }
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be empty");
        }

        try {
            logger.debug("Looking up user ID for username/email: {}", usernameOrEmail);

            // Check if input is an email
            boolean isEmail = usernameOrEmail.contains("@");
            List<KeycloakUserDto> users;

            if (isEmail) {
                // Search by email
                users = keycloakService.getUsersByEmail(keycloakProperties.getRealm(), usernameOrEmail,
                        "Bearer " + accessToken);
            } else {
                // Search by username
                users = keycloakService.getUsersByUsername(keycloakProperties.getRealm(), usernameOrEmail,
                        "Bearer " + accessToken);
            }

            if (users != null && !users.isEmpty()) {
                String userId = users.get(0).getId();
                logger.debug("Found user ID: {} for username/email: {}", userId, usernameOrEmail);
                return userId;
            }
            logger.debug("No user found with username/email: {}", usernameOrEmail);
            return null;
        } catch (Exception e) {
            logger.error("Failed to get user ID by username/email", e);
            throw new KeycloakIntegrationException("Error accessing Keycloak API", e);

        }
    }

    /**
     * Refresh authentication token
     *
     * @param refreshToken Refresh token
     * @return New authentication tokens
     * @throws AuthException if token refresh fails
     */
    public Map<String, Object> refreshToken(String refreshToken) throws AuthException {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            throw new AuthException("Refresh token cannot be empty");
        }

        try {
            logger.debug("Refreshing authentication token");

            // Call keycloakService with individual parameters instead of TokenRequestDto
            TokenRequestDto tokenRequest = TokenRequestDto.forRefreshToken(keycloakProperties.getClientId(),
                    keycloakProperties.getClientSecret(), refreshToken);

            KeycloakTokenResponse tokenResponse = keycloakService.getToken(keycloakProperties.getRealm(), tokenRequest);

            // Convert to map for API compatibility
            Map<String, Object> result = new HashMap<>();
            result.put("access_token", tokenResponse.getAccessToken());
            result.put("refresh_token", tokenResponse.getRefreshToken());
            result.put("expires_in", tokenResponse.getExpiresIn());
            result.put("refresh_expires_in", tokenResponse.getRefreshExpiresIn());
            result.put("token_type", tokenResponse.getTokenType());
            result.put("id_token", tokenResponse.getIdToken());
            result.put("not-before-policy", tokenResponse.getNotBeforePolicy());
            result.put("session_state", tokenResponse.getSessionState());
            result.put("scope", tokenResponse.getScope());

            return result;
        } catch (Exception e) {
            logger.error("Failed to refresh token", e);
            throw new AuthException("Invalid refresh token", e);

        }
    }

    public String getAdminAccessToken() {
        try {
            logger.debug("Getting admin access token");

            // Call keycloakService with individual parameters instead of TokenRequestDto
            TokenRequestDto tokenRequest = TokenRequestDto.forClientCredentials(keycloakProperties.getAdminClientId(),
                    keycloakProperties.getAdminClientSecret());
            KeycloakTokenResponse tokenResponse = keycloakService.getToken(keycloakProperties.getAdminRealm(),
                    tokenRequest);

            if (tokenResponse == null || tokenResponse.getAccessToken() == null) {
                throw new KeycloakIntegrationException("Admin token response is invalid");
            }

            return tokenResponse.getAccessToken();
        } catch (Exception e) {
            logger.error("Failed to get admin access token", e);
            throw new KeycloakIntegrationException("Invalid admin client credentials", e);
        }
    }

    /**
     * Get user ID by username from Keycloak
     *
     * @param username    Username to lookup
     * @param accessToken Admin access token
     * @return User ID or null if not found
     * @throws KeycloakIntegrationException if Keycloak API call fails
     */
    public String getUserIdByUsername(String username, String accessToken) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be empty");
        }

        try {
            logger.debug("Looking up user ID for username: {}", username);
            List<KeycloakUserDto> users = keycloakService.getUsersByUsername(keycloakProperties.getRealm(), username,
                    "Bearer " + accessToken);

            if (users != null && !users.isEmpty()) {
                String userId = users.get(0).getId();
                logger.debug("Found user ID: {} for username: {}", userId, username);
                return userId;
            }
            logger.debug("No user found with username: {}", username);
            return null;
        } catch (Exception e) {
            logger.error("Failed to get user ID by username", e);
            throw new KeycloakIntegrationException("Error accessing Keycloak API", e);
        }
    }

    /**
     * Get user information from Keycloak
     *
     * @param userId      User ID
     * @param accessToken Admin access token
     * @return User information as KeycloakUserDto
     * @throws KeycloakIntegrationException if Keycloak API call fails
     */
    public KeycloakUserDto getUserInfo(String userId, String accessToken) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be empty");
        }

        try {
            logger.debug("Getting user info for user ID: {}", userId);
            return keycloakService.getUserById(keycloakProperties.getRealm(), userId, "Bearer " + accessToken);
        } catch (Exception e) {
            logger.debug("User not found with ID: {}", userId);
            return null;
        }
//		} catch (Exception e) {
//            logger.error("Failed to get user info", e);
//            throw new KeycloakIntegrationException("Error accessing Keycloak API", e);
//        }
    }

    /**
     * Updates user information in both Keycloak and local database
     *
     * @param userId      The ID of the user to update
     * @param updates     Map containing the updates to apply
     * @param accessToken Admin access token for Keycloak
     * @return true if any updates were made, false if no changes were needed
     */
    public boolean updateUser(String userId, Map<String, Object> updates, String accessToken) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        if (updates == null || updates.isEmpty()) {
            logger.warn("No updates provided for user ID: {}", userId);
            return false; // Nothing to update
        }
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be empty");
        }

        try {
            logger.debug("Processing update request for user with ID: {}", userId);

            // Get user details from Keycloak
            KeycloakUserDto keycloakUser = getUserInfo(userId, accessToken);
            if (keycloakUser == null) {
                logger.error("User not found in Keycloak with ID: {}", userId);
                throw new ResourceNotFoundException("User not found in Keycloak with ID: " + userId);
            }

            // Extract user information
            String username = keycloakUser.getUsername();
            String email = keycloakUser.getEmail();
            String currentFirstName = keycloakUser.getFirstName();

            // First check if the user exists in our database, if yes update username/email
            // if needed
            User user = findAndUpdateUser(userId, username, email);
            boolean localDbUpdated = false;

            // If user doesn't exist, create a new one
            if (user == null) {
                logger.debug("User not found in local database, creating new user: {}", userId);
                // Convert KeycloakUserDto to Map for consistency with existing code
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", keycloakUser.getId());
                userInfo.put("username", keycloakUser.getUsername());
                userInfo.put("email", keycloakUser.getEmail());
                userInfo.put("firstName", keycloakUser.getFirstName());
                userInfo.put("lastName", keycloakUser.getLastName());

                user = createNonExistingUser(userInfo);
            }

            // Prepare Keycloak update payload
            KeycloakUserDto updatedUser = new KeycloakUserDto();
            boolean keycloakUpdateNeeded = false;

            // Handle name update
            if (updates.containsKey("name")) {
                String newName = (String) updates.get("name");
                if (newName != null && !newName.trim().isEmpty()) {
                    if (!newName.equals(currentFirstName)) {
                        updatedUser.setFirstName(newName);
                        keycloakUpdateNeeded = true;
                        logger.debug("Name will be updated from '{}' to '{}'", currentFirstName, newName);
                    } else {
                        logger.debug("Name update skipped - new value is the same as current value: '{}'",
                                currentFirstName);
                    }
                }
            }

            // Handle email update
            if (updates.containsKey("email")) {
                String newEmail = (String) updates.get("email");
                String currentEmail = keycloakUser.getEmail();

                if (newEmail != null && !newEmail.trim().isEmpty()) {
                    if (!newEmail.equals(currentEmail)) {
                        updatedUser.setEmail(newEmail);
                        keycloakUpdateNeeded = true;
                        logger.debug("Email will be updated in Keycloak from '{}' to '{}'", currentEmail, newEmail);

                        // Also update email in local database if it's different
                        if (!newEmail.equals(user.getEmail())) {
                            logger.debug("Email will be updated in local DB from '{}' to '{}'", user.getEmail(),
                                    newEmail);
                            user.setEmail(newEmail);
                            localDbUpdated = true;
                        }
                    } else {
                        logger.debug("Email update skipped - new value is the same as current value: '{}'",
                                currentEmail);
                    }
                }
            }

            // If we have updates for local database, save them
            if (localDbUpdated) {
                logger.debug("Updating user in local database: {}", user.getId());
                userRepository.save(user);
//				userEventPublisher.publishUserUpdated(UserMapper.toDto(user));
            } else {
                logger.debug("No changes needed for local database user: {}", user.getId());
            }

            // If we have updates for Keycloak, send them
            if (keycloakUpdateNeeded) {
                logger.debug("Updating user in Keycloak with changes: {}", updatedUser);
                keycloakService.updateUser(keycloakProperties.getRealm(), userId, updatedUser, "Bearer " + accessToken);
                return true;
            } else {
                logger.debug("No changes needed for Keycloak user: {}", userId);
                return false; // Indicate that no updates were made
            }
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update user in Keycloak", e);
            throw new KeycloakIntegrationException("Error updating user in Keycloak", e);
        }
    }

    /**
     * Register a new user in both Keycloak and local database with proper
     * transaction handling
     *
     * @param request   Personal details of the new user
     * @param principal The authenticated principal creating the user
     * @return The newly created user
     */
    @Transactional
    public User registerUser(PersonalDetailsDto request, Principal principal) {
        logger.info("Starting user registration for: {}", request.getEmail());
        String createdBy;
        String accessToken = getAdminAccessToken();
        String keycloakUserId = null;
        String existingKeycloakUserId = null;
        String tempPassword = null;
        User savedUser = null;
        boolean kafkaPublished = false;

        try {
            // Create user registration DTO
            UserRegistrationDto userRegistration = UserRegistrationDto.from(request.getEmail(), request.getEmail(),
                    request.getFirstName(), request.getLastName());

            // Build Keycloak user DTO
            UserRegistrationDto newUser = new UserRegistrationDto();
            String username = extractUsernameFromEmail(request.getEmail());
            newUser.setUsername(username);
            newUser.setEmail(request.getEmail());
            newUser.setEnabled(true);
            newUser.setFirstName(request.getFirstName());
            newUser.setLastName(request.getLastName());

            // Step 0: Check if user already exists in Keycloak or local DB
            boolean existsInKeycloak = false;
            boolean existsInLocalDB = userRepository.existsByEmail(request.getEmail());

            try {
                List<KeycloakUserDto> existingUsers = keycloakService.getUsersByUsername(keycloakProperties.getRealm(),
                        username, "Bearer " + accessToken);
                existingKeycloakUserId = existingUsers.get(0).getId();
                existsInKeycloak = (existingUsers != null && !existingUsers.isEmpty());
            } catch (Exception ex) {
                logger.warn("Error checking user in Keycloak: {}", ex.getMessage());
            }

            if (existsInKeycloak || existsInLocalDB) {
                throw new EmailAlreadyExistsException("Email is already registered.");
            }

            // Step 1: Create user in Keycloak
            try {
                logger.debug("Creating user in Keycloak: {}", request.getEmail());
                keycloakService.createUser(keycloakProperties.getRealm(), newUser, "Bearer " + accessToken);

                // Search and get Keycloak user ID
                List<KeycloakUserDto> users = keycloakService.getUsersByUsername(keycloakProperties.getRealm(), username,
                        "Bearer " + accessToken);

                if (users == null || users.isEmpty()) {
                    throw new RuntimeException("User not found in Keycloak after creation");
                }

                keycloakUserId = users.get(0).getId();
                logger.debug("User created in Keycloak with ID: {}", keycloakUserId);

                // Set temp password
                tempPassword = generateTemporaryPassword();
                logger.debug("Generated temporary password for user" + tempPassword);
                System.out.println("Generated temporary password for user" + tempPassword);

                Map<String, Object> credentialMap = new HashMap<>();
                credentialMap.put("type", "password");
                credentialMap.put("value", tempPassword);
                credentialMap.put("temporary", false);

                keycloakService.resetPassword(keycloakProperties.getRealm(), keycloakUserId, credentialMap,
                        "Bearer " + accessToken);
                logger.debug("Temporary password set in Keycloak");

                // Add roles if provided
                if (request.getRole() != null && !request.getRole().isEmpty()) {
                    assignKeycloakRoles(keycloakUserId, request.getRole(), accessToken);
                }
            } catch (Exception e) {
                logger.error("Failed to register user in Keycloak: {}", e.getMessage());
                throw new KeycloakIntegrationException("Error registering user in Keycloak: " + e.getMessage(), e);
            }

            // Step 2: Save user in local DB
            try {
                if (principal instanceof JwtAuthenticationToken jwtAuth) {
                    createdBy = (String) jwtAuth.getTokenAttributes().get("email");
                } else {
                    createdBy = principal.getName();
                }

                User localUser = new User();
                localUser.setId(keycloakUserId);
                localUser.setUsername(username);
                localUser.setEmail(request.getEmail());
                localUser.setFirstName(request.getFirstName());
                localUser.setLastName(request.getLastName());
                localUser.setCreatedBy(createdBy);

                String encodedPassword = passwordEncoder.encode(tempPassword);
                localUser.setTemporaryPassword(encodedPassword);
                localUser.setUpdatePassword(false);

                // Only add roles that exist in the request
                Set<UserRole> userRoles = new HashSet<>();
                if (request.getRole() != null) {
                    userRoles = request.getRole().stream().map(roleName -> {
                        UserRole role = new UserRole();
                        role.setRoleName(roleName);
                        role.setUser(localUser);
                        return role;
                    }).collect(Collectors.toSet());
                }

                localUser.setRoles(userRoles);

                logger.debug("Saving user to local database");
                savedUser = userRepository.save(localUser);
                logger.info("User saved in DB: {}", savedUser.getId());
            } catch (Exception dbEx) {
                logger.error("Failed to save user in local database", dbEx);
                // Clean up Keycloak since DB transaction will be rolled back
                cleanupKeycloak(keycloakUserId, accessToken);
                throw dbEx;
            }

            // Step 3: Publish to Kafka
            try {
                logger.debug("Publishing user created event to Kafka");
                request.setUserId(keycloakUserId);
                request.setUsername(username);
                userEventPublisher.publishUserCreated(request);
                kafkaPublished = true;
                logger.info("Successfully published user created event to Kafka");
            } catch (Exception kafkaEx) {
                logger.error("Failed to publish user created event to Kafka", kafkaEx);
                // Clean up Keycloak since we're going to force a transaction rollback
                cleanupKeycloak(keycloakUserId, accessToken);
                // Force transaction rollback by throwing an exception
                throw new RuntimeException("Failed to publish user created event to Kafka: " + kafkaEx.getMessage(),
                        kafkaEx);
            }

            return savedUser;
        } catch (Exception e) {
            // Any other unexpected error
            logger.error("Unexpected error during user registration", e);
            if (kafkaPublished) {
                keycloakUserId = existingKeycloakUserId;
                cleanupKeycloak(keycloakUserId, accessToken);
            }
            // Clean up Keycloak if necessary

            // Force transaction rollback
            throw new RuntimeException("Failed to register user: " + e.getMessage(), e);
        }
    }

    /**
     * Extract username from email (part before @)
     */
    private String extractUsernameFromEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        int atIndex = email.indexOf('@');
        if (atIndex == -1) {
            // If no @ symbol, return the whole email as username
            return email;
        }

        return email.substring(0, atIndex);
    }

    /**
     * Helper method to safely assign roles to a user in Keycloak
     */
    private void assignKeycloakRoles(String userId, Set<String> roleNames, String accessToken) {
        List<KeycloakRoleDto> rolesToAssign = new ArrayList<>();

        for (String roleName : roleNames) {
            try {
                // Use Optional for cleaner handling of potentially missing roles
                Optional<KeycloakRoleDto> roleOpt = getKeycloakRole(roleName, accessToken);
                if (roleOpt.isPresent()) {
                    rolesToAssign.add(roleOpt.get());

                } else {
                    // Create role if it doesn't exist
                    KeycloakRoleDto newRole = createKeycloakRole(roleName, accessToken);
                    rolesToAssign.add(newRole);
                    logger.info("Created and prepared new role: {}", roleName);
                }
            } catch (Exception e) {
                logger.warn("Error fetching role '{}': {}", roleName, e.getMessage());
                // Continue with other roles
            }
        }

        if (!rolesToAssign.isEmpty()) {
            try {
                keycloakService.assignRealmRoles(keycloakProperties.getRealm(), userId, rolesToAssign, "Bearer " + accessToken);
                logger.info("Assigned {} roles to user {}", rolesToAssign.size(), userId);
            } catch (Exception e) {
                logger.error("Failed to assign roles to user {}: {}", userId, e.getMessage());
                throw e; // Propagate to trigger rollback
            }
        }
    }

    /**
     * Get a Keycloak role with proper error handling
     *
     * @return Optional containing the role if found, empty Optional otherwise
     */
    private Optional<KeycloakRoleDto> getKeycloakRole(String roleName, String accessToken) {
        try {
            KeycloakRoleDto role = keycloakService.getRealmRole(keycloakProperties.getRealm(), roleName,
                    "Bearer " + accessToken);
            if (role != null) {
                return Optional.of(role);
            }
        } catch (Exception e) {
            logger.warn("Role '{}' not found in Keycloak: {}", roleName, e.getMessage());
        }
        return Optional.empty();
    }

    private KeycloakRoleDto createKeycloakRole(String roleName, String accessToken) {
        KeycloakRoleDto newRole = new KeycloakRoleDto();
        newRole.setName(roleName);
        keycloakService.createRealmRole(keycloakProperties.getRealm(), newRole, "Bearer " + accessToken);

        // Fetch it again to get the full metadata (id, etc.)
        return getKeycloakRole(roleName, accessToken)
                .orElseThrow(() -> new RuntimeException("Role creation failed for: " + roleName));
    }

    /**
     * Helper method to clean up Keycloak user in case of failure
     */
    private void cleanupKeycloak(String keycloakUserId, String accessToken) {
        if (keycloakUserId != null) {
            try {
                logger.info("Rolling back Keycloak user creation: {}", keycloakUserId);
                keycloakService.deleteUser(keycloakProperties.getRealm(), keycloakUserId, "Bearer " + accessToken);
                logger.info("Successfully rolled back Keycloak user");
            } catch (Exception cleanupEx) {
                logger.error("Failed to clean up Keycloak user after error: {}", cleanupEx.getMessage());
                // Just log, don't throw as this is cleanup code
            }
        }
    }

    /**
     * Generate a random temporary password
     *
     * @return A random password string
     */
    private String generateTemporaryPassword() {
        int length = new Random().nextInt(3) + 6; // Between 6 and 8
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, length);
    }

    /**
     * Update user password in both Keycloak and local database
     *
     * @param updaterEmail Email of the user updating the password
     * @param username     Username of the user whose password is being updated
     * @param passRequest  Password change request containing old and new passwords
     * @return ResponseEntity with status and message
     */
    public ResponseEntity<?> updatePassword(String updaterEmail, String username, PasswordChangeRequest passRequest) {
        String accessToken = getAdminAccessToken();

        Optional<User> userFromDb = userRepository.findByUsername(username);
        if (userFromDb.isEmpty()) {
            logger.warn("User not found in DB: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        User user = userFromDb.get();

        // Check old password matches
        if (user.getTemporaryPassword() != null) {
            if (!passwordEncoder.matches(passRequest.getOldPassword(), user.getTemporaryPassword())) {
                logger.warn("Password mismatch for user: {}", username);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password does not match");
            }
        }

        try {
            // Get User ID by username
            List<KeycloakUserDto> users = keycloakService.getUsersByUsername(keycloakProperties.getRealm(), username,
                    "Bearer " + accessToken);

            if (users == null || users.isEmpty()) {
                throw new RuntimeException("User not found: " + username);
            }

            String userId = users.get(0).getId();

            // Set new password
            Map<String, Object> passwordPayload = new HashMap<>();
            passwordPayload.put("type", "password");
            passwordPayload.put("value", passRequest.getNewPassword());
            passwordPayload.put("temporary", false);

            keycloakService.resetPassword(keycloakProperties.getRealm(), userId, passwordPayload,
                    "Bearer " + accessToken);

            user.setUpdatedBy(updaterEmail);
            user.setUpdatePassword(true);
            String encodedPassword = passwordEncoder.encode(passRequest.getNewPassword());
            user.setTemporaryPassword(encodedPassword);
            userRepository.save(user);

            logger.info("Password updated for userId: {}", userId);
            return ResponseEntity.ok("Password updated successfully");

        } catch (Exception e) {
            logger.error("Failed to update password in Keycloak", e);
            throw new KeycloakIntegrationException("Failed to update password in Keycloak. DB not updated.", e);

        }
    }
}

