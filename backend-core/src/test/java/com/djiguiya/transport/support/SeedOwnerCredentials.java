package com.djiguiya.transport.support;

/**
 * Expected owner identity after Flyway V2 + V3. Local/dev only — change in production.
 */
public final class SeedOwnerCredentials {

	public static final String USER_ID = "00000000-0000-0000-0000-000000000001";
	public static final String STAFF_ID = "00000000-0000-0000-0000-000000000101";
	public static final String EMAIL = "chakakone93@yahoo.fr";
	public static final String FULL_NAME = "Chaka Koné";
	public static final String ROLE_CODE = "owner";
	public static final String PASSWORD = "Djiguiya!Owner2026";
	public static final String PASSWORD_HASH = "$2b$10$dMTTwkcKzxUCkzYu/dyLXu5xaWfTUH3/umpDfecAtW7dYEvTxu6V2";

	/** Legacy V2 seed password (superseded by V3). */
	public static final String LEGACY_V2_PASSWORD = "password";
	public static final String LEGACY_V2_PASSWORD_HASH = "$2b$10$rzwZpDaQJv0vBIIZqHmYnOg8jqqo8il8wlMA1GaXMRiE.RcPsk/Fa";

	private SeedOwnerCredentials() {
	}
}
