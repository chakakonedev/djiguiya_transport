package com.djiguiya.transport.shared.persistence;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

/**
 * JPA callbacks that set {@link Timestamped} fields when callers leave them unset (seeders may set their own).
 */
public class AuditingListener {

	/** Sets both timestamps on first insert; preserves an explicit {@code createdAt} if already set. */
	@PrePersist
	public void onCreate(Timestamped entity) {
		Instant now = Instant.now();
		if (entity.getCreatedAt() == null) {
			entity.setCreatedAt(now);
		}
		entity.setUpdatedAt(now);
	}

	/** Refreshes {@code updatedAt} on every flush of a changed entity. */
	@PreUpdate
	public void onUpdate(Timestamped entity) {
		entity.setUpdatedAt(Instant.now());
	}
}
