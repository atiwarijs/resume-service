package com.cv.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Configuration
@ConfigurationProperties(prefix = "keycloak")
public class KeycloakConfig {

	private String realm;
	private String resource;
	private Credentials credentials = new Credentials();
	private String adminClientId;
	private String adminClientSecret;
	private String adminRealm;

	@Getter
	@Setter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Credentials {
		private String secret;

	}
}
