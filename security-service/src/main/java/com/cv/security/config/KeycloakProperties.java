package com.cv.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.cv.security.config.KeycloakConfig.Credentials;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Component
public class KeycloakProperties {

	@Value("${keycloak.realm}")
	private String realm;

	@Value("${keycloak.resource}")
	private String clientId;

	@Value("${keycloak.credentials.secret}")
	private String clientSecret;

	@Value("${keycloak.admin-client-id}")
	private String adminClientId;

	@Value("${keycloak.admin-client-secret}")
	private String adminClientSecret;

	@Value("${keycloak.admin-realm}")
	private String adminRealm;
}
