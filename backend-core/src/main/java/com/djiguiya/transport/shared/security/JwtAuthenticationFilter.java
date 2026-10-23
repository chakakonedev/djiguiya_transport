package com.djiguiya.transport.shared.security;

import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Parses {@code Authorization: Bearer} tokens and installs a Spring {@code Authentication}
 * (user id + {@code ROLE_*} from the live DB record). Invalid or revoked tokens clear the
 * context so protected routes return 401, not 500.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
	private final UserRepository userRepository;

	/** Non-blocking: always continues the chain; missing/bad JWT simply leaves the user unauthenticated. */
	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);
			try {
				Claims claims = jwtService.parse(token);
				String userId = claims.getSubject();
				UserEntity user = userRepository.findById(userId).orElse(null);
				if (user != null && user.isActive() && user.getRoleCode() != null && !user.getRoleCode().isBlank()) {
					var auth = new UsernamePasswordAuthenticationToken(
							userId,
							null,
							List.of(new SimpleGrantedAuthority("ROLE_" + user.getRoleCode().toUpperCase()))
					);
					SecurityContextHolder.getContext().setAuthentication(auth);
				} else {
					SecurityContextHolder.clearContext();
				}
			} catch (Exception ignored) {
				SecurityContextHolder.clearContext();
			}
		}
		filterChain.doFilter(request, response);
	}
}
