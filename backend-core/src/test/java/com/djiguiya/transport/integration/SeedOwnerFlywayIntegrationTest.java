package com.djiguiya.transport.integration;

import com.djiguiya.transport.config.CoreStaffDefinitions;
import com.djiguiya.transport.modules.auth.dto.ChangePasswordRequest;
import com.djiguiya.transport.modules.auth.dto.LoginRequest;
import com.djiguiya.transport.modules.finance.domain.ProfitShareRuleKinds;
import com.djiguiya.transport.modules.finance.repository.ProfitShareRuleRepository;
import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
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
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
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
	private ProfitShareRuleRepository profitShareRuleRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void flywaySeed_ownerIdentityAndLogin() throws Exception {
		UserEntity user = userRepository.findById(SeedOwnerCredentials.USER_ID)
				.orElseThrow(() -> new AssertionError("seed owner user missing after Flyway + seeder"));

		assertEquals(SeedOwnerCredentials.EMAIL, user.getEmail());
		assertEquals(SeedOwnerCredentials.FULL_NAME, user.getFullName());
		assertEquals(SeedOwnerCredentials.ROLE_CODE, user.getRoleCode());
		assertTrue(user.isActive());
		assertFalse(user.isPasswordChanged());
		assertTrue(passwordEncoder.matches(SeedOwnerCredentials.PASSWORD, user.getPasswordHash()));
		assertTrue(userRepository.findByEmailIgnoreCase(SeedOwnerCredentials.EMAIL).isPresent());

		var staff = staffRepository.findById(SeedOwnerCredentials.STAFF_ID)
				.orElseThrow(() -> new AssertionError("seed owner staff missing after Flyway + seeder"));
		assertEquals(SeedOwnerCredentials.FULL_NAME, staff.getFullName());
		assertEquals(SeedOwnerCredentials.USER_ID, staff.getUserId());
		assertNull(staff.getMonthlySalary());

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
	void flywaySeed_allCoreStaffPresent() {
		assertEquals(4, CoreStaffDefinitions.PEOPLE.size());
		for (CoreStaffDefinitions.Person person : CoreStaffDefinitions.PEOPLE) {
			UserEntity user = userRepository.findById(person.userId())
					.orElseThrow(() -> new AssertionError("missing user " + person.email()));
			assertEquals(person.email(), user.getEmail());
			assertEquals(person.fullName(), user.getFullName());
			assertEquals(person.roleCode(), user.getRoleCode());
			assertTrue(passwordEncoder.matches(SeedOwnerCredentials.PASSWORD, user.getPasswordHash()));

			StaffEntity staff = staffRepository.findById(person.staffId())
					.orElseThrow(() -> new AssertionError("missing staff " + person.fullName()));
			assertEquals(person.fullName(), staff.getFullName());
			assertEquals(person.roleCode(), staff.getStaffType());
			assertEquals(person.monthlySalary(), staff.getMonthlySalary());
			assertEquals(person.payDay(), staff.getPayDay());
			assertEquals(person.userId(), staff.getUserId());
		}

		StaffEntity manager = staffRepository.findById("00000000-0000-0000-0000-000000000102").orElseThrow();
		assertEquals(50_000L, manager.getMonthlySalary());
		assertEquals(28, manager.getPayDay());

		assertTrue(profitShareRuleRepository.findById("00000000-0000-0000-0000-000000000202")
				.filter(r -> ProfitShareRuleKinds.QUARTERLY_REVENUE_SHARE_PCT.equals(r.getRuleKind()))
				.filter(r -> r.getRatePercent().compareTo(new BigDecimal("1.0000")) == 0)
				.isPresent());
		assertEquals(4, CoreStaffDefinitions.SHARE_RULES.size());
		for (CoreStaffDefinitions.ShareRule rule : CoreStaffDefinitions.SHARE_RULES) {
			assertTrue(profitShareRuleRepository.findById(rule.id()).isPresent(), "missing rule " + rule.id());
		}
	}

	@Test
	void flywaySeed_legacyPasswordsRejected() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequest(SeedOwnerCredentials.EMAIL, SeedOwnerCredentials.LEGACY_V2_PASSWORD))))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequest(SeedOwnerCredentials.EMAIL, SeedOwnerCredentials.LEGACY_V3_PASSWORD))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void changePassword_thenLoginWithNewPassword() throws Exception {
		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequest(SeedOwnerCredentials.EMAIL, SeedOwnerCredentials.PASSWORD))))
				.andExpect(status().isOk())
				.andReturn();
		String token = objectMapper.readTree(login.getResponse().getContentAsString())
				.path("data").path("accessToken").asText();

		String newPassword = "MyNewSecure@Pass1";
		mockMvc.perform(post("/api/v1/auth/change-password")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new ChangePasswordRequest(SeedOwnerCredentials.PASSWORD, newPassword))))
				.andExpect(status().isOk());

		UserEntity user = userRepository.findById(SeedOwnerCredentials.USER_ID).orElseThrow();
		assertTrue(user.isPasswordChanged());
		assertTrue(passwordEncoder.matches(newPassword, user.getPasswordHash()));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequest(SeedOwnerCredentials.EMAIL, SeedOwnerCredentials.PASSWORD))))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								new LoginRequest(SeedOwnerCredentials.EMAIL, newPassword))))
				.andExpect(status().isOk());

		// Restore default for other tests in same JVM context if needed — re-encode default & clear flag
		user.setPasswordHash(passwordEncoder.encode(SeedOwnerCredentials.PASSWORD));
		user.setPasswordChanged(false);
		userRepository.save(user);
	}
}
