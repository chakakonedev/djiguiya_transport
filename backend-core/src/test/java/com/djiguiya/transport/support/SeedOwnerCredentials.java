package com.djiguiya.transport.support;

/**
 * Expected owner identity after Flyway + core staff seeder.
 * Default password comes from {@code djiguiya.security.default-password} (not a Flyway hash).
 */
public final class SeedOwnerCredentials {

	public static final String USER_ID = "00000000-0000-0000-0000-000000000001";
	public static final String STAFF_ID = "00000000-0000-0000-0000-000000000101";
	public static final String EMAIL = "chakakone93@yahoo.fr";
	public static final String FULL_NAME = "Chaka Koné";
	public static final String ROLE_CODE = "owner";
	/** Matches application default / flyway-test profile. */
	public static final String PASSWORD = "Djiguiya@2026";

	/** Legacy V2 seed password (superseded). */
	public static final String LEGACY_V2_PASSWORD = "password";
	public static final String LEGACY_V2_PASSWORD_HASH = "$2b$10$rzwZpDaQJv0vBIIZqHmYnOg8jqqo8il8wlMA1GaXMRiE.RcPsk/Fa";

	/** Legacy V3 Flyway hash password (superseded by seeder default). */
	public static final String LEGACY_V3_PASSWORD = "Djiguiya!Owner2026";
	public static final String LEGACY_V3_PASSWORD_HASH = "$2b$10$dMTTwkcKzxUCkzYu/dyLXu5xaWfTUH3/umpDfecAtW7dYEvTxu6V2";

	private SeedOwnerCredentials() {
	}
}
