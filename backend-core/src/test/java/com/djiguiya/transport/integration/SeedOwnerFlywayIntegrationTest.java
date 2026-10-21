package com.djiguiya.transport.integration;

import com.djiguiya.transport.modules.auth.dto.LoginRequest;
import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import com.djiguiya.transport.modules.staff.repository.StaffRepository;
import com.djiguiya.transport.support.SeedOwnerCredentials;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("flyway-test")
class SeedOwnerFlywayIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private StaffRepository staffRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void flywaySeed_ownerIdentityAndLogin() throws Exception {
		UserEntity user = userRepository.findById(SeedOwnerCredentials.USER_ID)
				.orElseThrow(() -> new AssertionError("seed owner user missing after Flyway V2+V3"));

		assertEquals(SeedOwnerCredentials.EMAIL, user.getEmail());
		assertEquals(SeedOwnerCredentials.FULL_NAME, user.getFullName());
		assertEquals(SeedOwnerCredentials.ROLE_CODE, user.getRoleCode());
		assertTrue(user.isActive());
		assertTrue(passwordEncoder.matches(SeedOwnerCredentials.PASSWORD, user.getPasswordHash()));
		assertTrue(userRepository.findByEmailIgnoreCase(SeedOwnerCredentials.EMAIL).isPresent());

		var staff = staffRepository.findById(SeedOwnerCredentials.STAFF_ID)
				.orElseThrow(() -> new AssertionError("seed owner staff missing after Flyway V2+V3"));
		assertEquals(SeedOwnerCredentials.FULL_NAME, staff.getFullName());
		assertEquals(SeedOwnerCredentials.USER_ID, staff.getUserId());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequest(SeedOwnerCredentials.EMAIL, SeedOwnerCredentials.PASSWORD))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.email").value(SeedOwnerCredentials.EMAIL))
				.andExpect(jsonPath("$.data.fullName").value(SeedOwnerCredentials.FULL_NAME))
				.andExpect(jsonPath("$.data.roleCode").value(SeedOwnerCredentials.ROLE_CODE))
				.andExpect(jsonPath("$.data.accessToken").isNotEmpty());
	}

	@Test
	void flywaySeed_legacyPasswordRejected() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequest(SeedOwnerCredentials.EMAIL, SeedOwnerCredentials.LEGACY_V2_PASSWORD))))
				.andExpect(status().isUnauthorized());
	}
}
