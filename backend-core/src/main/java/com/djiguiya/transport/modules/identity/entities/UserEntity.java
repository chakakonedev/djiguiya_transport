package com.djiguiya.transport.modules.identity.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Application login account. Linked optionally to {@link com.djiguiya.transport.modules.staff.entities.StaffEntity}
 * via {@code userId} on staff records; role drives API authorization.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class UserEntity {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false, unique = true, length = 180)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "full_name", nullable = false, length = 180)
	private String fullName;

	/** Role identifier consumed by {@code @PreAuthorize} expressions. */
	@Column(name = "role_code", nullable = false, length = 40)
	private String roleCode;

	@Column(nullable = false)
	private boolean active = true;

	/** False until the user sets a personal password (keeps bootstrap default). */
	@Column(name = "password_changed", nullable = false)
	private boolean passwordChanged = false;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
