package com.cv.security.service;

import com.cv.security.dto.PersonalDetailsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProfileService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${profile.service.url:http://localhost:8081}")
    private String profileServiceUrl;
    
    public PersonalDetailsDto getProfile(String userId) {
        String url = profileServiceUrl + "/profile/" + userId;
        
        try {
            ResponseEntity<PersonalDetailsDto> response = restTemplate.getForEntity(url, PersonalDetailsDto.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get profile: " + e.getMessage(), e);
        }
    }
    
    public PersonalDetailsDto getUserById(String userId) {
        return getProfile(userId);
    }
    
    public PersonalDetailsDto updateProfile(String userId, PersonalDetailsDto profile) {
        String url = profileServiceUrl + "/profile/" + userId;
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<PersonalDetailsDto> entity = new HttpEntity<>(profile, headers);
        
        try {
            ResponseEntity<PersonalDetailsDto> response = restTemplate.exchange(url, HttpMethod.PUT, entity, PersonalDetailsDto.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update profile: " + e.getMessage(), e);
        }
    }
}
