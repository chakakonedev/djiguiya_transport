package com.djiguiya.transport.integration;

import com.djiguiya.transport.modules.auth.dto.LoginRequest;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import com.djiguiya.transport.support.TestUserFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for fleet primary-driver validation and vehicle document API contract.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FleetPrimaryDriverIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private String accessToken;

	@BeforeEach
	void setUp() throws Exception {
		userRepository.deleteAll();
		TestUserFactory.createOwner(userRepository, passwordEncoder);

		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest("owner@test.local", "password"))))
				.andExpect(status().isOk())
				.andReturn();
		JsonNode root = objectMapper.readTree(login.getResponse().getContentAsString());
		accessToken = root.path("data").path("accessToken").asText();
	}

	/** Returns 400 when primary-driver assignment omits required startDate. */
	@Test
	void assignPrimaryDriver_requiresStartDate() throws Exception {
		String driverId = createDriver();
		String vehicleId = createVehicle();

		mockMvc.perform(post("/api/v1/vehicles/" + vehicleId + "/primary-driver")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"driverId":"%s"}
								""".formatted(driverId)))
				.andExpect(status().isBadRequest());
	}

	/** Succeeds and echoes assigned driver when startDate is provided. */
	@Test
	void assignPrimaryDriver_withStartDateSucceeds() throws Exception {
		String driverId = createDriver();
		String vehicleId = createVehicle();

		mockMvc.perform(post("/api/v1/vehicles/" + vehicleId + "/primary-driver")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"driverId":"%s","startDate":"2026-10-21"}
								""".formatted(driverId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data.primaryDriverId").value(driverId));
	}

	/** Accepts JSON array document payload matching the frontend shape. */
	@Test
	void replaceDocuments_acceptsArrayPayloadLikeFrontend() throws Exception {
		String vehicleId = createVehicle();

		mockMvc.perform(put("/api/v1/vehicles/" + vehicleId + "/documents")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								[
								  {
								    "docType":"insurance",
								    "expiresOn":"2027-06-01",
								    "lastRenewedOn":"2026-06-01",
								    "renewalPeriodMonths":12,
								    "lastAmount":200000
								  }
								]
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.data[0].docType").value("insurance"));
	}

	private String createDriver() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/staff")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"fullName":"Tièdiè Koné","staffType":"driver","status":"active"}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();
	}

	private String createVehicle() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/vehicles")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"bus1","plate":"DM-994-YH","status":"active"}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();
	}
}
