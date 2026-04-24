package com.cv.security.keycloak.dto;

import lombok.Data;

@Data
public class TokenRequestDto {
    private String grantType;
    private String clientId;
    private String clientSecret;
    private String username;
    private String password;
    private String refreshToken;
    private String scope;

    public static TokenRequestDto forPasswordGrant(String clientId, String clientSecret, String username, String password) {
        TokenRequestDto request = new TokenRequestDto();
        request.setGrantType("password");
        request.setClientId(clientId);
        request.setClientSecret(clientSecret);
        request.setUsername(username);
        request.setPassword(password);
        request.setScope("openid");
        return request;
    }

    public static TokenRequestDto forRefreshToken(String clientId, String clientSecret, String refreshToken) {
        TokenRequestDto request = new TokenRequestDto();
        request.setGrantType("refresh_token");
        request.setClientId(clientId);
        request.setClientSecret(clientSecret);
        request.setRefreshToken(refreshToken);
        request.setScope("openid");
        return request;
    }

    public static TokenRequestDto forClientCredentials(String clientId, String clientSecret) {
        TokenRequestDto request = new TokenRequestDto();
        request.setGrantType("client_credentials");
        request.setClientId(clientId);
        request.setClientSecret(clientSecret);
        request.setScope("openid");
        return request;
    }
}
