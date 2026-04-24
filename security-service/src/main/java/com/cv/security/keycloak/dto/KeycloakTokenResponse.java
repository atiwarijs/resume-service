package com.cv.security.keycloak.dto;

import lombok.Data;

@Data
public class KeycloakTokenResponse {
    private String accessToken;
    private String refreshToken;
    private int expiresIn;
    private int refreshExpiresIn;
    private String tokenType;
    private String idToken;
    private String notBeforePolicy;
    private String sessionState;
    private String scope;
}
