package com.djiguiya.transport.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Read-only access to the authenticated user id (JWT subject) from the security context.
 */
public final class SecurityUtils {

	private SecurityUtils() {
	}

	/** Empty when the request is anonymous or not yet authenticated. */
	public static Optional<String> currentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
			return Optional.empty();
		}
		return Optional.of(auth.getName());
	}
}
