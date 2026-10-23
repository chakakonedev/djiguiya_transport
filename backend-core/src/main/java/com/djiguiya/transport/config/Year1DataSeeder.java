package com.djiguiya.transport.config;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.modules.fleet.entities.PrimaryAssignmentEntity;
import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;
import com.djiguiya.transport.modules.fleet.repository.PrimaryAssignmentRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleRepository;
import com.djiguiya.transport.modules.trips.entities.TripEntity;
import com.djiguiya.transport.modules.trips.repository.TripRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Imports year-1 ledger data extracted from {@code dashboard.html} + CSV detail.
 * Idempotent: skips when the year-1 vehicle already exists.
 */
@Component
@Order(200)
@RequiredArgsConstructor
@Slf4j
public class Year1DataSeeder implements ApplicationRunner {

	private static final String SEED_RESOURCE = "seed/year1_seed.json";

	private final CoreStaffSeedProperties properties;
	private final ObjectMapper objectMapper;
	private final VehicleRepository vehicleRepository;
	private final PrimaryAssignmentRepository primaryAssignmentRepository;
	private final TripRepository tripRepository;
	private final TransactionRepository transactionRepository;

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		if (!properties.getSeed().isYear1Enabled()) {
			return;
		}

		ClassPathResource resource = new ClassPathResource(SEED_RESOURCE);
		if (!resource.exists()) {
			throw new IllegalStateException("Missing classpath resource: " + SEED_RESOURCE
					+ " — run: python scripts/extract_dashboard_to_app.py");
		}

		JsonNode root;
		try (InputStream in = resource.getInputStream()) {
			root = objectMapper.readTree(in);
		}

		String vehicleId = text(root.path("vehicle").path("id"));
		Instant now = Instant.now();
		if (vehicleRepository.existsById(vehicleId)) {
			// Keep identity fields aligned with seed (plate / purchase) without re-importing ledger.
			syncVehicleIdentity(root.path("vehicle"), now);
			log.info("Year-1 seed skipped — vehicle {} already present (identity synced)", vehicleId);
			return;
		}

		upsertVehicle(root.path("vehicle"), now);
		upsertAssignment(root.path("primary_assignment"));

		String createdBy = textOrNull(root.path("created_by"));
		int tripCount = 0;
		int revenueCount = 0;
		for (JsonNode tripNode : root.path("trips")) {
			saveTrip(tripNode, now);
			saveRevenue(tripNode, createdBy, now);
			tripCount++;
			revenueCount++;
		}

		int expenseCount = 0;
		for (JsonNode expenseNode : root.path("expenses")) {
			saveExpense(expenseNode, createdBy, now);
			expenseCount++;
		}

		JsonNode ledger = root.path("ledger_year");
		log.info(
				"Year-1 seed applied: {} trips, {} revenues, {} expenses (ledger R={} D={} S={})",
				tripCount,
				revenueCount,
				expenseCount,
				ledger.path("revenue").asLong(),
				ledger.path("expenses").asLong(),
				ledger.path("savings").asLong());
	}

	private void upsertVehicle(JsonNode node, Instant now) {
		VehicleEntity vehicle = new VehicleEntity();
		vehicle.setId(text(node.path("id")));
		applyVehicleFields(vehicle, node);
		vehicle.setCreatedAt(now);
		vehicle.setUpdatedAt(now);
		vehicleRepository.save(vehicle);
	}

	private void syncVehicleIdentity(JsonNode node, Instant now) {
		String id = text(node.path("id"));
		VehicleEntity vehicle = vehicleRepository.findById(id).orElseThrow(
				() -> new IllegalStateException("Year-1 vehicle missing during identity sync: " + id));
		applyVehicleFields(vehicle, node);
		vehicle.setUpdatedAt(now);
		vehicleRepository.save(vehicle);
	}

	private void applyVehicleFields(VehicleEntity vehicle, JsonNode node) {
		vehicle.setName(text(node.path("name")));
		vehicle.setPlate(textOrNull(node.path("plate")));
		vehicle.setBrand(textOrNull(node.path("brand")));
		vehicle.setModel(textOrNull(node.path("model")));
		if (!node.path("year_built").isNull() && !node.path("year_built").isMissingNode()) {
			vehicle.setYearBuilt(node.path("year_built").asInt());
		} else {
			vehicle.setYearBuilt(null);
		}
		if (!node.path("purchase_price").isNull() && !node.path("purchase_price").isMissingNode()) {
			vehicle.setPurchasePrice(node.path("purchase_price").asLong());
		} else {
			vehicle.setPurchasePrice(null);
		}
		String purchaseDate = textOrNull(node.path("purchase_date"));
		vehicle.setPurchaseDate(purchaseDate != null ? LocalDate.parse(purchaseDate) : null);
		vehicle.setStatus(text(node.path("status")));
		vehicle.setNotes(textOrNull(node.path("notes")));
	}

	private void upsertAssignment(JsonNode node) {
		PrimaryAssignmentEntity assignment = new PrimaryAssignmentEntity();
		assignment.setId(text(node.path("id")));
		assignment.setVehicleId(text(node.path("vehicle_id")));
		assignment.setDriverId(text(node.path("driver_id")));
		assignment.setStartDate(LocalDate.parse(text(node.path("start_date"))));
		String endDate = textOrNull(node.path("end_date"));
		assignment.setEndDate(endDate != null ? LocalDate.parse(endDate) : null);
		assignment.setReason(textOrNull(node.path("reason")));
		primaryAssignmentRepository.save(assignment);
	}

	private void saveTrip(JsonNode node, Instant now) {
		LocalDate date = LocalDate.parse(text(node.path("date")));
		TripEntity trip = new TripEntity();
		trip.setId(text(node.path("id")));
		trip.setVehicleId(text(node.path("vehicle_id")));
		trip.setDepartureDate(date);
		trip.setReturnDate(date);
		trip.setMonthKey(text(node.path("month_key")));
		trip.setDriverId(text(node.path("driver_id")));
		trip.setReplacement(false);
		trip.setNotes(textOrNull(node.path("notes")));
		trip.setCreatedAt(now);
		trip.setUpdatedAt(now);
		tripRepository.save(trip);
	}

	private void saveRevenue(JsonNode tripNode, String createdBy, Instant now) {
		LocalDate date = LocalDate.parse(text(tripNode.path("date")));
		TransactionEntity txn = new TransactionEntity();
		txn.setId(text(tripNode.path("revenue_txn_id")));
		txn.setType(TransactionTypes.REVENUE);
		txn.setTxnDate(date);
		txn.setMonthKey(text(tripNode.path("month_key")));
		txn.setAmount(tripNode.path("amount").asLong());
		txn.setVehicleId(text(tripNode.path("vehicle_id")));
		txn.setDriverId(text(tripNode.path("driver_id")));
		txn.setTripId(text(tripNode.path("id")));
		txn.setLabel("Versement voyage");
		txn.setNotes(textOrNull(tripNode.path("notes")));
		txn.setCreatedBy(createdBy);
		txn.setCreatedAt(now);
		txn.setUpdatedAt(now);
		transactionRepository.save(txn);
	}

	private void saveExpense(JsonNode node, String createdBy, Instant now) {
		TransactionEntity txn = new TransactionEntity();
		txn.setId(text(node.path("id")));
		txn.setType(TransactionTypes.EXPENSE);
		txn.setTxnDate(LocalDate.parse(text(node.path("date"))));
		txn.setMonthKey(text(node.path("month_key")));
		txn.setAmount(node.path("amount").asLong());
		txn.setVehicleId(textOrNull(node.path("vehicle_id")));
		txn.setDriverId(textOrNull(node.path("driver_id")));
		txn.setCategoryCode(text(node.path("category_code")));
		txn.setSubcategoryCode(text(node.path("subcategory_code")));
		txn.setLabel(text(node.path("label")));
		txn.setPayrollMonth(textOrNull(node.path("payroll_month")));
		txn.setPayrollKind(textOrNull(node.path("payroll_kind")));
		txn.setNotes(textOrNull(node.path("notes")));
		txn.setCreatedBy(createdBy);
		txn.setCreatedAt(now);
		txn.setUpdatedAt(now);
		transactionRepository.save(txn);
	}

	private static String text(JsonNode node) {
		String value = textOrNull(node);
		if (value == null) {
			throw new IllegalStateException("Missing required JSON text field");
		}
		return value;
	}

	private static String textOrNull(JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode()) {
			return null;
		}
		String value = node.asText();
		return value == null || value.isBlank() || "null".equals(value) ? null : value;
	}
}
