package com.djiguiya.transport.shared.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

	private final SecretKey key;
	private final long accessExpirationMs;

	public JwtService(
			@Value("${djiguiya.jwt.secret}") String secret,
			@Value("${djiguiya.jwt.access-token-expiration-ms}") long accessExpirationMs
	) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessExpirationMs = accessExpirationMs;
	}

	public String generateAccessToken(String userId, String email, String roleCode) {
		Date now = new Date();
		return Jwts.builder()
				.subject(userId)
				.claims(Map.of("email", email, "role", roleCode))
				.issuedAt(now)
				.expiration(new Date(now.getTime() + accessExpirationMs))
				.signWith(key)
				.compact();
	}

	public Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
