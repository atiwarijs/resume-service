package com.cv.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SecurityServiceApplication {

	public static void main(String[] args) {
		System.setProperty("spring.application.name", "SECURITY-SERVICE");
		SpringApplication.run(SecurityServiceApplication.class, args);
	}
}
