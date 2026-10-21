package com.djiguiya.transport.support;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedPasswordHashTest {

	private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

	@Test
	void v3PasswordHashMatchesSeedPassword() {
		assertTrue(encoder.matches(SeedOwnerCredentials.PASSWORD, SeedOwnerCredentials.PASSWORD_HASH));
	}

	@Test
	void legacyV2PasswordHashStillMatchesPassword() {
		assertTrue(encoder.matches(SeedOwnerCredentials.LEGACY_V2_PASSWORD, SeedOwnerCredentials.LEGACY_V2_PASSWORD_HASH));
	}
}
