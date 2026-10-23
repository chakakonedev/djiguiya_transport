package com.djiguiya.transport;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the Djiguiya transport backend (REST API, persistence, security).
 */
@SpringBootApplication
public class DjiguiyaTransportApplication {

	/** Boots the application context and embedded web server. */
	public static void main(String[] args) {
		SpringApplication.run(DjiguiyaTransportApplication.class, args);
	}
}
