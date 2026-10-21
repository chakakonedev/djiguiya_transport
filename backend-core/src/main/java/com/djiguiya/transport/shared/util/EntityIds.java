package com.djiguiya.transport.shared.util;

import java.util.UUID;

public final class EntityIds {

	private EntityIds() {
	}

	public static String newId() {
		return UUID.randomUUID().toString();
	}
}
