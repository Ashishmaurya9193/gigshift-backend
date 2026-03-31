package com.gigplatform.user_service;



import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(unique = true, nullable = false)
	private String email;

	@JsonIgnore
	private String password;

	@Enumerated(EnumType.STRING)
	private AuthProvider provider;

	private String providerId;

	public User() {
	}

	public User(Long id, String name, String email, String password) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
	}

	public User(Long id, String name, String email, String password, AuthProvider provider, String providerId) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.provider = provider;
		this.providerId = providerId;
	}

}
