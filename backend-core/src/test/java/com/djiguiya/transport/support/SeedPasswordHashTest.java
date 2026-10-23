package com.djiguiya.transport.support;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests that seeded legacy BCrypt hashes in {@link SeedOwnerCredentials} still match their passwords.
 */
class SeedPasswordHashTest {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	/** Confirms the v2 legacy seed password verifies against its stored hash. */
	@Test
	void legacyV2PasswordHashStillMatchesPassword() {
		assertTrue(encoder.matches(SeedOwnerCredentials.LEGACY_V2_PASSWORD, SeedOwnerCredentials.LEGACY_V2_PASSWORD_HASH));
	}

	/** Confirms the v3 legacy seed password verifies against its stored hash. */
	@Test
	void legacyV3PasswordHashStillMatchesPassword() {
		assertTrue(encoder.matches(SeedOwnerCredentials.LEGACY_V3_PASSWORD, SeedOwnerCredentials.LEGACY_V3_PASSWORD_HASH));
	}
}
