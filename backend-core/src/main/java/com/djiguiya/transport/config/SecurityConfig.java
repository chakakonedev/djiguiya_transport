package com.djiguiya.transport.config;

import com.djiguiya.transport.shared.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HTTP security: stateless JWT authentication, CORS for the SPA, and BCrypt password hashing.
 * Login, health, and (when enabled) API docs are anonymous; all other routes require a Bearer token.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Value("${djiguiya.security.cors-allowed-origins}")
	private String corsAllowedOrigins;

	@Value("${djiguiya.security.openapi-enabled:true}")
	private boolean openapiEnabled;

	private static final String[] ALWAYS_PUBLIC = {
			"/api/v1/auth/login",
			"/api/v1/ping",
			"/actuator/health"
	};

	private static final String[] OPENAPI_PUBLIC = {
			"/swagger-ui/**",
			"/swagger-ui.html",
			"/api-docs/**",
			"/v3/api-docs/**"
	};

	/** Stateless chain: JWT filter before username/password; CSRF off (token-based API). */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		String[] publicEndpoints = publicEndpoints();
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(publicEndpoints).permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	private String[] publicEndpoints() {
		List<String> endpoints = new ArrayList<>(Arrays.asList(ALWAYS_PUBLIC));
		if (openapiEnabled) {
			endpoints.addAll(Arrays.asList(OPENAPI_PUBLIC));
		}
		return endpoints.toArray(String[]::new);
	}

	/** CORS for {@code /api/**}; allowed origins from {@code djiguiya.security.cors-allowed-origins}. */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(Arrays.stream(corsAllowedOrigins.split(","))
				.map(String::trim)
				.filter(o -> !o.isEmpty())
				.toList());
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", config);
		return source;
	}

	/** Shared encoder for login, password change, and seed/bootstrap accounts. */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/** Exposed for {@code AuthService} credential checks against the security stack. */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
