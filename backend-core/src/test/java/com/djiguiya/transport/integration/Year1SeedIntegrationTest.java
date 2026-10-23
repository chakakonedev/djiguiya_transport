package com.djiguiya.transport.integration;

import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleRepository;
import com.djiguiya.transport.modules.trips.repository.TripRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"djiguiya.seed.core-staff-enabled=true",
		"djiguiya.seed.year1-enabled=true"
})
class Year1SeedIntegrationTest {

	@Autowired
	private VehicleRepository vehicleRepository;
	@Autowired
	private TripRepository tripRepository;
	@Autowired
	private TransactionRepository transactionRepository;

	@Test
	void year1DataIsLoadedIntoDatabase() {
		assertTrue(vehicleRepository.existsById("00000000-0000-0000-0000-000000000301"));

		long trips = tripRepository.findByDeletedAtIsNullOrderByDepartureDateDesc().size();
		long txns = transactionRepository.findByDeletedAtIsNullOrderByTxnDateDesc().size();

		assertEquals(183, trips);
		// 183 revenues + 382 expenses
		assertEquals(565, txns);

		long revenues = transactionRepository.findByDeletedAtIsNullOrderByTxnDateDesc().stream()
				.filter(t -> "revenue".equals(t.getType()))
				.mapToLong(t -> t.getAmount())
				.sum();
		long expenses = transactionRepository.findByDeletedAtIsNullOrderByTxnDateDesc().stream()
				.filter(t -> "expense".equals(t.getType()))
				.mapToLong(t -> t.getAmount())
				.sum();

		assertEquals(24_250_000L, revenues);
		assertEquals(11_352_500L, expenses);
	}
}
