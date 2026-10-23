package com.djiguiya.transport.config;

import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;
import com.djiguiya.transport.modules.fleet.repository.PrimaryAssignmentRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleRepository;
import com.djiguiya.transport.modules.trips.entities.TripEntity;
import com.djiguiya.transport.modules.trips.repository.TripRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.core.io.ClassPathResource;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Year1DataSeeder}: classpath seed resource, feature flag, and import/sync paths with mocks.
 */
@ExtendWith(MockitoExtension.class)
class Year1DataSeederTest {

	@Mock
	private VehicleRepository vehicleRepository;
	@Mock
	private PrimaryAssignmentRepository primaryAssignmentRepository;
	@Mock
	private TripRepository tripRepository;
	@Mock
	private TransactionRepository transactionRepository;

	private CoreStaffSeedProperties properties;
	private Year1DataSeeder seeder;

	@BeforeEach
	void setUp() {
		properties = new CoreStaffSeedProperties();
		seeder = new Year1DataSeeder(
				properties,
				new ObjectMapper(),
				vehicleRepository,
				primaryAssignmentRepository,
				tripRepository,
				transactionRepository);
	}

	/** Ensures the year-one JSON seed file is on the test classpath. */
	@Test
	void seedResourceIsPresent() {
		assertTrue(new ClassPathResource("seed/year1_seed.json").exists());
	}

	/** Does not touch repositories when year-one seeding is disabled. */
	@Test
	void skipsWhenDisabled() throws Exception {
		properties.getSeed().setYear1Enabled(false);

		seeder.run(new DefaultApplicationArguments());

		verify(vehicleRepository, never()).save(any());
		verify(tripRepository, never()).save(any());
	}

	/** Updates plate and purchase fields only when the seed vehicle id already exists. */
	@Test
	void syncsVehicleIdentityWhenAlreadyPresent() throws Exception {
		properties.getSeed().setYear1Enabled(true);
		String vehicleId = "00000000-0000-0000-0000-000000000301";
		VehicleEntity existing = new VehicleEntity();
		existing.setId(vehicleId);
		existing.setName("Bus 1");
		existing.setStatus("active");
		when(vehicleRepository.existsById(vehicleId)).thenReturn(true);
		when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(existing));

		seeder.run(new DefaultApplicationArguments());

		ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
		verify(vehicleRepository).save(vehicleCaptor.capture());
		assertEquals("AA-496-ER", vehicleCaptor.getValue().getPlate());
		assertEquals(25_000_000L, vehicleCaptor.getValue().getPurchasePrice());
		assertEquals(LocalDate.of(2025, 7, 1), vehicleCaptor.getValue().getPurchaseDate());
		verify(tripRepository, never()).save(any());
		verify(transactionRepository, never()).save(any());
	}

	/** Imports full year-one dataset when enabled and the anchor vehicle is absent. */
	@Test
	void importsSeedWhenEnabledAndVehicleMissing() throws Exception {
		properties.getSeed().setYear1Enabled(true);
		when(vehicleRepository.existsById("00000000-0000-0000-0000-000000000301")).thenReturn(false);

		seeder.run(new DefaultApplicationArguments());

		ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
		verify(vehicleRepository).save(vehicleCaptor.capture());
		assertEquals("Bus 1", vehicleCaptor.getValue().getName());
		assertEquals("AA-496-ER", vehicleCaptor.getValue().getPlate());
		assertEquals(25_000_000L, vehicleCaptor.getValue().getPurchasePrice());
		assertEquals(LocalDate.of(2025, 7, 1), vehicleCaptor.getValue().getPurchaseDate());

		verify(primaryAssignmentRepository).save(any());
		verify(tripRepository, atLeast(180)).save(any(TripEntity.class));
		verify(transactionRepository, atLeast(500)).save(any(TransactionEntity.class));

		ArgumentCaptor<TripEntity> tripCaptor = ArgumentCaptor.forClass(TripEntity.class);
		verify(tripRepository, atLeast(1)).save(tripCaptor.capture());
		assertEquals("source=year1_import", tripCaptor.getAllValues().getFirst().getNotes());
	}
}
