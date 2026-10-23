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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

	@Test
	void seedResourceIsPresent() {
		assertTrue(new ClassPathResource("seed/year1_seed.json").exists());
	}

	@Test
	void skipsWhenDisabled() throws Exception {
		properties.getSeed().setYear1Enabled(false);

		seeder.run(new DefaultApplicationArguments());

		verify(vehicleRepository, never()).save(any());
		verify(tripRepository, never()).save(any());
	}

	@Test
	void skipsWhenVehicleAlreadyPresent() throws Exception {
		properties.getSeed().setYear1Enabled(true);
		when(vehicleRepository.existsById("00000000-0000-0000-0000-000000000301")).thenReturn(true);

		seeder.run(new DefaultApplicationArguments());

		verify(vehicleRepository, never()).save(any());
		verify(tripRepository, never()).save(any());
	}

	@Test
	void importsSeedWhenEnabledAndVehicleMissing() throws Exception {
		properties.getSeed().setYear1Enabled(true);
		when(vehicleRepository.existsById("00000000-0000-0000-0000-000000000301")).thenReturn(false);

		seeder.run(new DefaultApplicationArguments());

		ArgumentCaptor<VehicleEntity> vehicleCaptor = ArgumentCaptor.forClass(VehicleEntity.class);
		verify(vehicleRepository).save(vehicleCaptor.capture());
		assertEquals("Bus 1", vehicleCaptor.getValue().getName());

		verify(primaryAssignmentRepository).save(any());
		verify(tripRepository, atLeast(180)).save(any(TripEntity.class));
		verify(transactionRepository, atLeast(500)).save(any(TransactionEntity.class));

		ArgumentCaptor<TripEntity> tripCaptor = ArgumentCaptor.forClass(TripEntity.class);
		verify(tripRepository, atLeast(1)).save(tripCaptor.capture());
		assertEquals("source=year1_import", tripCaptor.getAllValues().getFirst().getNotes());
	}
}
