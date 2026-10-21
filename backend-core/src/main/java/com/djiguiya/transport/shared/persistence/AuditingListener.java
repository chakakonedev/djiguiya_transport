package com.djiguiya.transport.shared.persistence;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.Instant;

public class AuditingListener {

	@PrePersist
	public void onCreate(Timestamped entity) {
		Instant now = Instant.now();
		if (entity.getCreatedAt() == null) {
			entity.setCreatedAt(now);
		}
		entity.setUpdatedAt(now);
	}

	@PreUpdate
	public void onUpdate(Timestamped entity) {
		entity.setUpdatedAt(Instant.now());
	}
}
