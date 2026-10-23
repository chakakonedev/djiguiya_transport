package com.djiguiya.transport.shared.persistence;

import java.time.Instant;

/**
 * Entities that expose creation and last-update instants; stamped by {@link AuditingListener} on persist/update.
 */
public interface Timestamped {

	Instant getCreatedAt();

	void setCreatedAt(Instant createdAt);

	Instant getUpdatedAt();

	void setUpdatedAt(Instant updatedAt);
}
