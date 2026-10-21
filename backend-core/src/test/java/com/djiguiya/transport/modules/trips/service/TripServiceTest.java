package com.djiguiya.transport.modules.trips.service;

import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;
import com.djiguiya.transport.modules.fleet.service.VehicleService;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.service.StaffService;
import com.djiguiya.transport.modules.trips.dto.TripRequest;
import com.djiguiya.transport.modules.trips.repository.TripRepository;
import com.djiguiya.transport.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TripServiceTest {

	@Mock
	private TripRepository tripRepository;
	@Mock
	private VehicleService vehicleService;
	@Mock
	private StaffService staffService;
	@InjectMocks
	private TripService tripService;

	@Test
	void create_usesPrimaryDriverWhenDriverMissing() {
		VehicleEntity vehicle = new VehicleEntity();
		vehicle.setId("v1");
		vehicle.setName("Bus 1");
		when(vehicleService.findVehicle("v1")).thenReturn(vehicle);
		when(vehicleService.resolvePrimaryDriverId("v1", LocalDate.of(2026, 3, 1)))
				.thenReturn(Optional.of("d1"));
		StaffEntity driver = new StaffEntity();
		driver.setId("d1");
		driver.setFullName("Amadou");
		when(staffService.findEntity("d1")).thenReturn(driver);
		when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		var request = new TripRequest("v1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), null, false, null, null);
		var response = tripService.create(request);

		assertEquals("d1", response.driverId());
		assertEquals("Amadou", response.driverName());
		assertEquals("Bus 1", response.vehicleName());
		assertEquals("2026-03", response.monthKey());

		ArgumentCaptor<com.djiguiya.transport.modules.trips.entities.TripEntity> captor =
				ArgumentCaptor.forClass(com.djiguiya.transport.modules.trips.entities.TripEntity.class);
		verify(tripRepository).save(captor.capture());
		assertEquals("2026-03", captor.getValue().getMonthKey());
	}

	@Test
	void create_failsWithoutPrimaryDriver() {
		when(vehicleService.findVehicle("v1")).thenReturn(new VehicleEntity());
		when(vehicleService.resolvePrimaryDriverId(any(), any())).thenReturn(Optional.empty());

		var request = new TripRequest("v1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), null, false, null, null);
		assertThrows(BusinessException.class, () -> tripService.create(request));
	}

	@Test
	void create_failsWhenDepartureAfterReturn() {
		var request = new TripRequest("v1", LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 1), null, false, null, null);
		assertThrows(BusinessException.class, () -> tripService.create(request));
	}

	@Test
	void create_usesExplicitDriverWhenProvided() {
		VehicleEntity vehicle = new VehicleEntity();
		vehicle.setId("v1");
		vehicle.setName("Bus 1");
		when(vehicleService.findVehicle("v1")).thenReturn(vehicle);
		StaffEntity driver = new StaffEntity();
		driver.setId("d2");
		driver.setFullName("Sekou");
		when(staffService.findEntity("d2")).thenReturn(driver);
		when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		var request = new TripRequest("v1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), "d2", false, null, null);
		var response = tripService.create(request);

		assertEquals("d2", response.driverId());
		assertEquals("Sekou", response.driverName());
	}
}
