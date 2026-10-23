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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Broad MockMvc integration coverage: staff, payroll, transactions, stats, profit-share, fleet, and trips.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ControllersCoverageIntegrationTest {

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

	/** Hits staff CRUD, payroll, transactions, stats, profit-share, health, and validation error paths. */
	@Test
	void staffPayrollTransactionsStatsAndHealthEndpoints() throws Exception {
		String staffJson = """
				{"fullName":"Cashier One","staffType":"cashier","phone":"70000001","status":"active","monthlySalary":100000,"payDay":25}
				""";
		MvcResult staffResult = mockMvc.perform(post("/api/v1/staff")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(staffJson))
				.andExpect(status().isOk())
				.andReturn();
		String staffId = objectMapper.readTree(staffResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		mockMvc.perform(get("/api/v1/staff")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray());

		mockMvc.perform(get("/api/v1/staff/" + staffId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.fullName").value("Cashier One"));

		String updateStaff = """
				{"fullName":"Cashier Updated","staffType":"cashier","phone":"70000001","status":"active","monthlySalary":120000,"payDay":20}
				""";
		mockMvc.perform(put("/api/v1/staff/" + staffId)
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateStaff))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.fullName").value("Cashier Updated"));

		String salaryJson = """
				{"staffId":"%s","amount":120000,"payrollMonth":"2026-03","txnDate":"2026-03-28","label":"Salaire mars"}
				""".formatted(staffId);
		mockMvc.perform(post("/api/v1/payroll/salary")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(salaryJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.categoryCode").value("payroll"));

		String bonusJson = """
				{"staffId":"%s","amount":20000,"payrollMonth":"2026-03","txnDate":"2026-03-28"}
				""".formatted(staffId);
		MvcResult bonusResult = mockMvc.perform(post("/api/v1/payroll/bonus")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(bonusJson))
				.andExpect(status().isOk())
				.andReturn();
		String txnId = objectMapper.readTree(bonusResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		mockMvc.perform(get("/api/v1/transactions")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray());

		mockMvc.perform(get("/api/v1/transactions/" + txnId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(txnId));

		String expenseUpdate = """
				{"type":"expense","txnDate":"2026-03-28","amount":25000,"driverId":"%s","categoryCode":"payroll","subcategoryCode":"bonus","label":"Bonus updated","payrollMonth":"2026-03","payrollKind":"bonus"}
				""".formatted(staffId);
		mockMvc.perform(put("/api/v1/transactions/" + txnId)
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(expenseUpdate))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.amount").value(25000));

		mockMvc.perform(delete("/api/v1/transactions/" + txnId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/stats/monthly")
						.header("Authorization", "Bearer " + accessToken)
						.param("year", "2025"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.months").isArray());

		mockMvc.perform(get("/api/v1/stats/compare-month")
						.header("Authorization", "Bearer " + accessToken)
						.param("monthIndex", "3"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/stats/yoy")
						.header("Authorization", "Bearer " + accessToken)
						.param("year", "2025"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/profit-share/preview")
						.header("Authorization", "Bearer " + accessToken)
						.param("year", "2025"))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/profit-share/rules")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/ping")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.status").value("ok"));

		mockMvc.perform(post("/api/v1/staff")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.messageKey").value("common.validation_failed"));
	}

	/** Covers vehicle/trip CRUD, primary driver assignment, and vehicle document endpoints. */
	@Test
	void vehicleAndTripCrud_coversControllers() throws Exception {
		String staffJson = """
				{"fullName":"Driver Two","staffType":"driver","status":"active"}
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
				{"name":"Bus Cover","plate":"CV-01","status":"active","purchasePrice":1000,"brand":"Mercedes"}
				""";
		MvcResult vehicleResult = mockMvc.perform(post("/api/v1/vehicles")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(vehicleJson))
				.andExpect(status().isOk())
				.andReturn();
		String vehicleId = objectMapper.readTree(vehicleResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		mockMvc.perform(get("/api/v1/vehicles")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/vehicles/" + vehicleId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("Bus Cover"));

		String updateVehicle = """
				{"name":"Bus Cover 2","plate":"CV-01","status":"active","purchasePrice":2000}
				""";
		mockMvc.perform(put("/api/v1/vehicles/" + vehicleId)
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateVehicle))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.name").value("Bus Cover 2"));

		String assignJson = """
				{"driverId":"%s","startDate":"2026-01-01"}
				""".formatted(driverId);
		mockMvc.perform(post("/api/v1/vehicles/" + vehicleId + "/primary-driver")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(assignJson))
				.andExpect(status().isOk());

		String tripJson = """
				{"vehicleId":"%s","departureDate":"2026-03-01","returnDate":"2026-03-03"}
				""".formatted(vehicleId);
		MvcResult tripResult = mockMvc.perform(post("/api/v1/trips")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(tripJson))
				.andExpect(status().isOk())
				.andReturn();
		String tripId = objectMapper.readTree(tripResult.getResponse().getContentAsString())
				.path("data").path("id").asText();

		mockMvc.perform(get("/api/v1/trips")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		mockMvc.perform(get("/api/v1/trips/" + tripId)
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());

		String updateTrip = """
				{"vehicleId":"%s","departureDate":"2026-03-01","returnDate":"2026-03-04","driverId":"%s","notes":"extended"}
				""".formatted(vehicleId, driverId);
		mockMvc.perform(put("/api/v1/trips/" + tripId)
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(updateTrip))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data.notes").value("extended"));

		String docsJson = """
				[{"docType":"insurance","expiresOn":"2027-01-01","lastRenewedOn":"2026-01-01","renewalPeriodMonths":12,"lastAmount":150000}]
				""";
		mockMvc.perform(put("/api/v1/vehicles/" + vehicleId + "/documents")
						.header("Authorization", "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(docsJson))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data[0].docType").value("insurance"));

		mockMvc.perform(get("/api/v1/vehicles/" + vehicleId + "/documents")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.data").isArray());
	}
}
