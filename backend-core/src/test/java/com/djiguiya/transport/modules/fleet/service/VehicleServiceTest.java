package com.djiguiya.transport.modules.fleet.service;

import com.djiguiya.transport.modules.fleet.dto.PrimaryDriverRequest;
import com.djiguiya.transport.modules.fleet.dto.VehicleDocumentRequest;
import com.djiguiya.transport.modules.fleet.entities.PrimaryAssignmentEntity;
import com.djiguiya.transport.modules.fleet.entities.VehicleDocumentEntity;
import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;
import com.djiguiya.transport.modules.fleet.repository.PrimaryAssignmentRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleDocumentRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleRepository;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.service.StaffService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link VehicleService} primary-driver assignment and document replacement; mocked repos.
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

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

	/** Saves a new open-ended assignment with the requested start date. */
	@Test
	void assignPrimaryDriver_persistsRequiredStartDate() {
		VehicleEntity vehicle = vehicle("v1");
		when(vehicleRepository.findById("v1")).thenReturn(Optional.of(vehicle));
		when(staffService.findEntity("d1")).thenReturn(staff("d1"));
		when(primaryAssignmentRepository.findByVehicleIdAndEndDateIsNullOrderByStartDateAsc("v1"))
				.thenReturn(List.of());
		when(primaryAssignmentRepository.findActiveForVehicleOnDate(eq("v1"), any()))
				.thenReturn(Optional.empty());
		when(primaryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		LocalDate start = LocalDate.of(2026, 10, 21);
		vehicleService.assignPrimaryDriver("v1", new PrimaryDriverRequest("d1", start, null, null));

		ArgumentCaptor<PrimaryAssignmentEntity> captor = ArgumentCaptor.forClass(PrimaryAssignmentEntity.class);
		verify(primaryAssignmentRepository).save(captor.capture());
		assertEquals("d1", captor.getValue().getDriverId());
		assertEquals("v1", captor.getValue().getVehicleId());
		assertEquals(start, captor.getValue().getStartDate());
		assertNull(captor.getValue().getEndDate());
	}

	/** Ends the prior assignment the day before the new start and stores reason on create. */
	@Test
	void assignPrimaryDriver_usesProvidedStartDateAndClosesPrevious() {
		VehicleEntity vehicle = vehicle("v1");
		PrimaryAssignmentEntity previous = new PrimaryAssignmentEntity();
		previous.setId("a0");
		previous.setVehicleId("v1");
		previous.setDriverId("d0");
		previous.setStartDate(LocalDate.of(2025, 1, 1));
		previous.setEndDate(null);

		when(vehicleRepository.findById("v1")).thenReturn(Optional.of(vehicle));
		when(staffService.findEntity("d1")).thenReturn(staff("d1"));
		when(primaryAssignmentRepository.findByVehicleIdAndEndDateIsNullOrderByStartDateAsc("v1"))
				.thenReturn(List.of(previous));
		when(primaryAssignmentRepository.findActiveForVehicleOnDate(eq("v1"), any()))
				.thenReturn(Optional.of(previous));
		when(primaryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		LocalDate start = LocalDate.of(2026, 3, 15);
		vehicleService.assignPrimaryDriver("v1", new PrimaryDriverRequest("d1", start, null, "rotation"));

		assertEquals(LocalDate.of(2026, 3, 14), previous.getEndDate());

		ArgumentCaptor<PrimaryAssignmentEntity> captor = ArgumentCaptor.forClass(PrimaryAssignmentEntity.class);
		verify(primaryAssignmentRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
		PrimaryAssignmentEntity created = captor.getAllValues().stream()
				.filter(a -> "d1".equals(a.getDriverId()))
				.findFirst()
				.orElseThrow();
		assertEquals(start, created.getStartDate());
		assertEquals("rotation", created.getReason());
	}

	/** Closes the previous assignment on the same day when reassigning with today's start. */
	@Test
	void assignPrimaryDriver_sameDayReassignmentClosesPrevious() {
		VehicleEntity vehicle = vehicle("v1");
		LocalDate today = LocalDate.of(2026, 10, 23);
		PrimaryAssignmentEntity previous = new PrimaryAssignmentEntity();
		previous.setId("a0");
		previous.setVehicleId("v1");
		previous.setDriverId("d0");
		previous.setStartDate(today);
		previous.setEndDate(null);

		when(vehicleRepository.findById("v1")).thenReturn(Optional.of(vehicle));
		when(staffService.findEntity("d1")).thenReturn(staff("d1"));
		when(primaryAssignmentRepository.findByVehicleIdAndEndDateIsNullOrderByStartDateAsc("v1"))
				.thenReturn(List.of(previous));
		when(primaryAssignmentRepository.findActiveForVehicleOnDate(eq("v1"), any()))
				.thenReturn(Optional.of(previous));
		when(primaryAssignmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		vehicleService.assignPrimaryDriver("v1", new PrimaryDriverRequest("d1", today, null, null));

		assertEquals(today, previous.getEndDate());
	}

	/** Inserts new vehicle document rows and returns mapped DTOs. */
	@Test
	void replaceDocuments_upsertsEachEntry() {
		VehicleEntity vehicle = vehicle("v1");
		when(vehicleRepository.findById("v1")).thenReturn(Optional.of(vehicle));
		when(vehicleDocumentRepository.findByVehicleIdAndDocType("v1", "insurance"))
				.thenReturn(Optional.empty());
		when(vehicleDocumentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		var docs = vehicleService.replaceDocuments(
				"v1",
				List.of(new VehicleDocumentRequest(
						"insurance",
						LocalDate.of(2027, 1, 1),
						LocalDate.of(2026, 1, 1),
						12,
						150000L,
						null)));

		assertEquals(1, docs.size());
		assertEquals("insurance", docs.get(0).docType());
		ArgumentCaptor<VehicleDocumentEntity> captor = ArgumentCaptor.forClass(VehicleDocumentEntity.class);
		verify(vehicleDocumentRepository).save(captor.capture());
		assertEquals("v1", captor.getValue().getVehicleId());
		assertEquals(LocalDate.of(2027, 1, 1), captor.getValue().getExpiresOn());
	}

	private static VehicleEntity vehicle(String id) {
		VehicleEntity entity = new VehicleEntity();
		entity.setId(id);
		entity.setName("Bus");
		entity.setStatus("active");
		return entity;
	}

	private static StaffEntity staff(String id) {
		StaffEntity entity = new StaffEntity();
		entity.setId(id);
		entity.setFullName("Driver");
		return entity;
	}
}
