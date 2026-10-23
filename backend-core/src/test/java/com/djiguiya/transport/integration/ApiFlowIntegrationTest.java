package com.djiguiya.transport.integration;

import com.djiguiya.transport.modules.auth.dto.LoginRequest;
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

import com.djiguiya.transport.modules.identity.repository.UserRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API integration test: authenticated flow from staff/vehicle through trip revenue to stats.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFlowIntegrationTest {

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

	/** Exercises create/assign/post revenue and confirms monthly summary reflects revenue. */
	@Test
	void fullFlow_vehicleStaffTripRevenueAndStats() throws Exception {
		String staffJson = """
				{"fullName":"Driver One","staffType":"driver","phone":"70000000","status":"active"}
				""";
		MvcResult staffResult = mockMvc.perform(post("/api/v1/staff")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(staffJson))
				.andExpect(status().isOk())
				.andReturn();
		String driverId = objectMapper.readTree(staffResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		String vehicleJson = """
				{"name":"Truck A","plate":"AB-123","status":"active"}
				""";
		MvcResult vehicleResult = mockMvc.perform(post("/api/v1/vehicles")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(vehicleJson))
				.andExpect(status().isOk())
				.andReturn();
		String vehicleId = objectMapper.readTree(vehicleResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		String assignJson = """
				{"driverId":"%s","startDate":"2026-01-01"}
				""".formatted(driverId);
		mockMvc.perform(post("/api/v1/vehicles/" + vehicleId + "/primary-driver")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(assignJson))
				.andExpect(status().isOk());

		String tripJson = """
				{"vehicleId":"%s","departureDate":"2026-03-10","returnDate":"2026-03-12"}
				""".formatted(vehicleId);
		MvcResult tripResult = mockMvc.perform(post("/api/v1/trips")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(tripJson))
				.andExpect(status().isOk())
				.andReturn();
		String tripId = objectMapper.readTree(tripResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		String revenueJson = """
				{"type":"revenue","txnDate":"2026-03-12","amount":500000,"vehicleId":"%s","driverId":"%s","tripId":"%s","label":"Trip fare"}
				""".formatted(vehicleId, driverId, tripId);
		mockMvc.perform(post("/api/v1/transactions")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(revenueJson))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/stats/summary")
						.header("Authorization", "Bearer " + accessToken)
						.param("month", "3")
						.param("year", "2026")
						.param("vehicleId", vehicleId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalRevenue").value(500000));
	}
}
