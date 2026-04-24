package com.cv.security.keycloak.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled = true;

    public static UserRegistrationDto from(String username, String email, String firstName, String lastName) {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        return dto;
    }
}
