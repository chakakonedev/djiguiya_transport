package com.djiguiya.transport.modules.trips.service;

import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;
import com.djiguiya.transport.modules.fleet.service.VehicleService;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.service.StaffService;
import com.djiguiya.transport.modules.trips.dto.TripRequest;
import com.djiguiya.transport.modules.trips.entities.TripEntity;
import com.djiguiya.transport.modules.trips.repository.TripRepository;
import com.djiguiya.transport.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TripService} list/get/update and entity lookup; Mockito collaborators.
 */
@ExtendWith(MockitoExtension.class)
class TripServiceExtraTest {

	@Mock
	private TripRepository tripRepository;
	@Mock
	private VehicleService vehicleService;
	@Mock
	private StaffService staffService;
	@InjectMocks
	private TripService tripService;

	/** Lists trips with enriched names and applies update fields including replacement flag. */
	@Test
	void listAll_andGetById_andUpdate() {
		TripEntity entity = new TripEntity();
		entity.setId("trip-1");
		entity.setVehicleId("v1");
		entity.setDriverId("d1");
		entity.setDepartureDate(LocalDate.of(2026, 3, 1));
		entity.setReturnDate(LocalDate.of(2026, 3, 3));
		entity.setMonthKey("2026-03");

		VehicleEntity vehicle = new VehicleEntity();
		vehicle.setId("v1");
		vehicle.setName("Bus 1");
		StaffEntity driver = new StaffEntity();
		driver.setId("d1");
		driver.setFullName("Amadou");

		when(tripRepository.findByDeletedAtIsNullOrderByDepartureDateDesc()).thenReturn(List.of(entity));
		when(tripRepository.findByIdAndDeletedAtIsNull("trip-1")).thenReturn(Optional.of(entity));
		when(vehicleService.findVehicle("v1")).thenReturn(vehicle);
		when(staffService.findEntity("d1")).thenReturn(driver);
		when(tripRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		assertEquals(1, tripService.listAll().size());
		assertEquals("Amadou", tripService.getById("trip-1").driverName());

		var updated = tripService.update(
				"trip-1",
				new TripRequest("v1", LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 5), "d1", true, "rempla", "note"));
		assertEquals("note", updated.notes());
		assertEquals(true, updated.replacement());
	}

	/** Throws when findEntity is called for a soft-deleted or missing trip. */
	@Test
	void findEntity_notFound() {
		when(tripRepository.findByIdAndDeletedAtIsNull("x")).thenReturn(Optional.empty());
		assertThrows(BusinessException.class, () -> tripService.findEntity("x"));
	}
}
