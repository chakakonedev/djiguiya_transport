package com.djiguiya.transport.modules.staff.service;

import com.djiguiya.transport.modules.staff.dto.StaffRequest;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.repository.StaffRepository;
import com.djiguiya.transport.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link StaffService} CRUD mapping and defaults; mocked {@link StaffRepository}.
 */
@ExtendWith(MockitoExtension.class)
class StaffServiceTest {

	@Mock
	private StaffRepository staffRepository;
	@InjectMocks
	private StaffService staffService;

	/** Maps persisted staff entities to response DTOs in list order. */
	@Test
	void listAll_mapsEntities() {
		StaffEntity entity = staff("s1", "Amadou");
		when(staffRepository.findAll()).thenReturn(List.of(entity));

		var list = staffService.listAll();
		assertEquals(1, list.size());
		assertEquals("Amadou", list.getFirst().fullName());
	}

	/** Throws when the requested staff id does not exist. */
	@Test
	void getById_notFound() {
		when(staffRepository.findById("missing")).thenReturn(Optional.empty());
		assertThrows(BusinessException.class, () -> staffService.getById("missing"));
	}

	/** Trims names and defaults blank status to active on create. */
	@Test
	void create_defaultsStatusToActive() {
		when(staffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		StaffRequest request = new StaffRequest(
				"  Diakaridia  ", "driver", "7000", null, 200_000L, 25, null, "note");
		var created = staffService.create(request);

		assertEquals("Diakaridia", created.fullName());
		assertEquals("active", created.status());
		assertEquals(200_000L, created.monthlySalary());

		ArgumentCaptor<StaffEntity> captor = ArgumentCaptor.forClass(StaffEntity.class);
		verify(staffRepository).save(captor.capture());
		assertEquals("driver", captor.getValue().getStaffType());
	}

	/** Applies all mutable fields from the request on update. */
	@Test
	void update_appliesFields() {
		StaffEntity entity = staff("s1", "Old");
		when(staffRepository.findById("s1")).thenReturn(Optional.of(entity));
		when(staffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		StaffRequest request = new StaffRequest(
				"New Name", "cashier", null, "inactive", 100L, 10, "u1", null);
		var updated = staffService.update("s1", request);

		assertEquals("New Name", updated.fullName());
		assertEquals("inactive", updated.status());
		assertEquals("u1", updated.userId());
	}

	private static StaffEntity staff(String id, String name) {
		StaffEntity entity = new StaffEntity();
		entity.setId(id);
		entity.setFullName(name);
		entity.setStaffType("driver");
		entity.setStatus("active");
		return entity;
	}
}
