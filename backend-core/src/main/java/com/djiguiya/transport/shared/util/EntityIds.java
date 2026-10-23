package com.djiguiya.transport.shared.util;

import java.util.UUID;

/**
 * String UUID primary keys for new domain rows (aligned with API and JPA id type).
 */
public final class EntityIds {

	private EntityIds() {
	}

	public static String newId() {
		return UUID.randomUUID().toString();
	}
}
