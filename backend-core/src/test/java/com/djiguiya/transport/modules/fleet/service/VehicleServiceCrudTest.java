package com.djiguiya.transport.modules.fleet.service;

import com.djiguiya.transport.modules.fleet.dto.VehicleRequest;
import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;
import com.djiguiya.transport.modules.fleet.repository.PrimaryAssignmentRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleDocumentRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleRepository;
import com.djiguiya.transport.modules.staff.service.StaffService;
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
 * Unit tests for {@link VehicleService} create/list/get/update and lookup errors; Mockito only.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceCrudTest {

	@Mock
	private VehicleRepository vehicleRepository;
	@Mock
	private PrimaryAssignmentRepository primaryAssignmentRepository;
	@Mock
	private VehicleDocumentRepository vehicleDocumentRepository;
	@Mock
	private StaffService staffService;
	@InjectMocks
	private VehicleService vehicleService;

	/** Exercises happy-path create, list, get-by-id, and update for vehicles. */
	@Test
	void create_list_get_update() {
		when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
		when(primaryAssignmentRepository.findActiveForVehicleOnDate(any(), any())).thenReturn(Optional.empty());

		var created = vehicleService.create(new VehicleRequest(
				"Bus X", "PL-1", "Brand", "Model", 2020, 1_000_000L, LocalDate.of(2020, 1, 1), null, "n"));
		assertEquals("Bus X", created.name());
		assertEquals("active", created.status());

		VehicleEntity entity = new VehicleEntity();
		entity.setId("v1");
		entity.setName("Bus X");
		entity.setStatus("active");
		when(vehicleRepository.findAll()).thenReturn(List.of(entity));
		when(vehicleRepository.findById("v1")).thenReturn(Optional.of(entity));

		assertEquals(1, vehicleService.listAll().size());
		assertEquals("Bus X", vehicleService.getById("v1").name());

		var updated = vehicleService.update(
				"v1",
				new VehicleRequest("Bus Y", "PL-2", null, null, null, null, null, "inactive", null));
		assertEquals("Bus Y", updated.name());
		assertEquals("inactive", updated.status());
	}

	/** Throws when findVehicle is called for a missing id. */
	@Test
	void findVehicle_notFound() {
		when(vehicleRepository.findById("missing")).thenReturn(Optional.empty());
		assertThrows(BusinessException.class, () -> vehicleService.findVehicle("missing"));
	}
}
