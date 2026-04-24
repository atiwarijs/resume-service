package com.cv.security.entity;

import java.util.HashSet;
import java.util.Set;

import com.cv.security.audit.Auditable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable {

	@Id
	@Column(name = "id", nullable = false, length = 36, unique = true)
	private String id; // Should match Keycloak 'sub' claim

	@Column(nullable = false, unique = true)
	private String username;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

//	@Convert(converter = Encrypt.class)
	@Column(name = "password")
	private String temporaryPassword;

	@Column(name = "UPDATE_PASSWORD")
	private boolean updatePassword;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<UserRole> roles = new HashSet<>();

	@Column(nullable = false)
	private String status = "ACTIVE"; // Optional status flag
}
