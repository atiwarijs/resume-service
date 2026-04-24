package com.cv.security.service;

import com.cv.security.keycloak.dto.KeycloakRoleDto;
import com.cv.security.keycloak.dto.KeycloakTokenResponse;
import com.cv.security.keycloak.dto.KeycloakUserDto;
import com.cv.security.keycloak.dto.TokenRequestDto;
import com.cv.security.keycloak.dto.UserRegistrationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    @Value("${keycloak.client-id}")
    private String clientId;
    
    @Value("${keycloak.client-secret}")
    private String clientSecret;
    
    public KeycloakTokenResponse getToken(String realm, TokenRequestDto tokenRequest) {
        String url = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        
        HttpEntity<TokenRequestDto> entity = new HttpEntity<>(tokenRequest, headers);
        
        return restTemplate.postForObject(url, entity, KeycloakTokenResponse.class);
    }
    
    public KeycloakUserDto createUser(String realm, UserRegistrationDto userRegistration, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        
        HttpEntity<UserRegistrationDto> entity = new HttpEntity<>(userRegistration, headers);
        
        ResponseEntity<KeycloakUserDto> response = restTemplate.postForEntity(url, entity, KeycloakUserDto.class);
        return response.getBody();
    }
    
    public List<KeycloakUserDto> getUsersByUsername(String realm, String username, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users?username=" + username;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        return response.getBody();
    }
    
    public List<KeycloakUserDto> getUsersByEmail(String realm, String email, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users?email=" + email;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
        return response.getBody();
    }
    
    public KeycloakUserDto getUserById(String realm, String userId, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(url, HttpMethod.GET, entity, KeycloakUserDto.class).getBody();
    }
    
    public void updateUser(String realm, String userId, KeycloakUserDto user, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        
        HttpEntity<KeycloakUserDto> entity = new HttpEntity<>(user, headers);
        
        restTemplate.put(url, entity);
    }
    
    public void resetPassword(String realm, String userId, Map<String, Object> passwordPayload, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/reset-password";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(passwordPayload, headers);
        
        restTemplate.put(url, entity);
    }
    
    public void deleteUser(String realm, String userId, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
    }
    
    public void assignRealmRoles(String realm, String userId, List<KeycloakRoleDto> roles, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        
        HttpEntity<List<KeycloakRoleDto>> entity = new HttpEntity<>(roles, headers);
        
        restTemplate.postForLocation(url, entity);
    }
    
    public KeycloakRoleDto getRealmRole(String realm, String roleName, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + roleName;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        
        HttpEntity<String> entity = new HttpEntity<>(headers);
        
        return restTemplate.exchange(url, HttpMethod.GET, entity, KeycloakRoleDto.class).getBody();
    }
    
    public void createRealmRole(String realm, KeycloakRoleDto role, String accessToken) {
        String url = keycloakServerUrl + "/admin/realms/" + realm + "/roles";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);
        
        HttpEntity<KeycloakRoleDto> entity = new HttpEntity<>(role, headers);
        
        restTemplate.postForLocation(url, entity);
    }
    
        
    private String getAdminToken() {
        TokenRequestDto tokenRequest = TokenRequestDto.forClientCredentials(clientId, clientSecret);
        KeycloakTokenResponse response = getToken(realm, tokenRequest);
        return response != null ? response.getAccessToken() : null;
    }
}
