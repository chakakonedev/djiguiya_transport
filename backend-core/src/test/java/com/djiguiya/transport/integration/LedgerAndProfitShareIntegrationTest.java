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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LedgerAndProfitShareIntegrationTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private String token;
	private String vehicleId;
	private String driverId;
	private String tripId;

	@BeforeEach
	void setUp() throws Exception {
		userRepository.deleteAll();
		TestUserFactory.createOwner(userRepository, passwordEncoder);

		MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new LoginRequest("owner@test.local", "password"))))
				.andExpect(status().isOk())
				.andReturn();
		token = objectMapper.readTree(login.getResponse().getContentAsString())
				.path("data").path("accessToken").asText();

		driverId = createStaff();
		vehicleId = createVehicle();
		assignPrimary(driverId, vehicleId);
		tripId = createTrip(vehicleId);
	}

	@Test
	void revenueRequiresTrip_expenseCreatesCategoryBreakdown() throws Exception {
		mockMvc.perform(post("/api/v1/transactions")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"revenue","txnDate":"2026-03-10","amount":100000,"label":"Orphan"}
								"""))
				.andExpect(status().isBadRequest());

		mockMvc.perform(post("/api/v1/transactions")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"revenue","txnDate":"2026-03-12","amount":1000000,"vehicleId":"%s","driverId":"%s","tripId":"%s","label":"Fare"}
								""".formatted(vehicleId, driverId, tripId)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/v1/transactions")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"expense","txnDate":"2026-03-12","amount":200000,"vehicleId":"%s","categoryCode":"bus","subcategoryCode":"tires","label":"Tires"}
								""".formatted(vehicleId)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/stats/summary")
						.header("Authorization", "Bearer " + token)
						.param("year", "2026")
						.param("month", "3")
						.param("vehicleId", vehicleId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.totalRevenue").value(1000000))
				.andExpect(jsonPath("$.data.totalExpense").value(200000))
				.andExpect(jsonPath("$.data.netProfit").value(800000));
	}

	@Test
	void compareMonthAndYoyAndProfitPreview_returnOk() throws Exception {
		mockMvc.perform(post("/api/v1/transactions")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"type":"revenue","txnDate":"2026-03-12","amount":800000,"vehicleId":"%s","driverId":"%s","tripId":"%s","label":"Fare"}
								""".formatted(vehicleId, driverId, tripId)))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/stats/compare-month")
						.header("Authorization", "Bearer " + token)
						.param("monthIndex", "3"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/stats/yoy")
						.header("Authorization", "Bearer " + token)
						.param("year", "2026"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/profit-share/preview")
						.header("Authorization", "Bearer " + token)
						.param("year", "2026"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.year").value(2026));
	}

	private String createStaff() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/staff")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"fullName":"Amadou","staffType":"driver","status":"active","monthlySalary":150000,"payDay":28}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();
	}

	private String createVehicle() throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/vehicles")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"Bus 1","plate":"ML-01","status":"active","purchasePrice":15000000,"purchaseDate":"2025-01-01"}
								"""))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();
	}

	private void assignPrimary(String driverId, String vehicleId) throws Exception {
		mockMvc.perform(post("/api/v1/vehicles/" + vehicleId + "/primary-driver")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"driverId":"%s","startDate":"2026-01-01"}
								""".formatted(driverId)))
				.andExpect(status().isOk());
	}

	private String createTrip(String vehicleId) throws Exception {
		MvcResult result = mockMvc.perform(post("/api/v1/trips")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"vehicleId":"%s","departureDate":"2026-03-10","returnDate":"2026-03-12"}
								""".formatted(vehicleId)))
				.andExpect(status().isOk())
				.andReturn();
		return objectMapper.readTree(result.getResponse().getContentAsString()).path("data").path("id").asText();
	}
}
