package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.dto.TransactionRequest;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.modules.trips.entities.TripEntity;
import com.djiguiya.transport.modules.trips.service.TripService;
import com.djiguiya.transport.shared.exception.BusinessException;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TransactionService} validation, enrichment, CRUD, and soft delete; mocked repos.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@Mock
	private TransactionRepository transactionRepository;
	@Mock
	private TripService tripService;
	@InjectMocks
	private TransactionService transactionService;

	/** Rejects expense with subcategory other when label and notes are both absent. */
	@Test
	void create_rejectsOtherSubcategoryWithoutLabelAndNotes() {
		TransactionRequest request = expenseOther(null, null);

		BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.create(request));
		assertEquals("transaction.other_requires_details", ex.getMessageKey());
	}

	/** Allows other subcategory when both label and notes are provided. */
	@Test
	void create_acceptsOtherSubcategoryWithLabelAndNotes() {
		TransactionRequest request = expenseOther("Réparation spéciale", "Remplacement câble");
		when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		transactionService.create(request);

		verify(transactionRepository).save(any(TransactionEntity.class));
	}

	/** Rejects revenue rows that omit tripId. */
	@Test
	void create_rejectsRevenueWithoutTrip() {
		TransactionRequest request = new TransactionRequest(
				"revenue", LocalDate.of(2026, 3, 12), 500_000L, null, null, null,
				null, null, "Fare", null, null, null);

		BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.create(request));
		assertEquals("transaction.revenue_requires_trip", ex.getMessageKey());
	}

	/** Rejects revenue dated before the linked trip return date. */
	@Test
	void create_rejectsRevenueBeforeTripReturn() {
		TripEntity trip = trip("trip-1", "v1", "d1", LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12));
		when(tripService.findEntity("trip-1")).thenReturn(trip);

		TransactionRequest request = new TransactionRequest(
				"revenue", LocalDate.of(2026, 3, 11), 500_000L, null, null, "trip-1",
				null, null, "Fare", null, null, null);

		BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.create(request));
		assertEquals("transaction.payment_before_trip", ex.getMessageKey());
	}

	/** Copies vehicle and driver from trip and sets default revenue label and month key. */
	@Test
	void create_enrichesVehicleAndDriverFromTrip() {
		TripEntity trip = trip("trip-1", "v1", "d1", LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 12));
		when(tripService.findEntity("trip-1")).thenReturn(trip);
		when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		TransactionRequest request = new TransactionRequest(
				"revenue", LocalDate.of(2026, 3, 12), 500_000L, null, null, "trip-1",
				null, null, null, null, null, null);

		var response = transactionService.create(request);

		assertEquals("v1", response.vehicleId());
		assertEquals("d1", response.driverId());
		assertEquals("Versement voyage", response.label());

		ArgumentCaptor<TransactionEntity> captor = ArgumentCaptor.forClass(TransactionEntity.class);
		verify(transactionRepository).save(captor.capture());
		assertEquals("2026-03", captor.getValue().getMonthKey());
	}

	/** Rejects expenses missing top-level categoryCode. */
	@Test
	void create_rejectsExpenseWithoutCategory() {
		TransactionRequest request = new TransactionRequest(
				"expense", LocalDate.of(2026, 3, 12), 10_000L, null, null, null,
				null, "fuel", "Fuel", null, null, null);

		BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.create(request));
		assertEquals("common.validation_failed", ex.getMessageKey());
	}

	/** Lists active transactions, fetches by id, and soft-deletes by setting deletedAt. */
	@Test
	void listAll_andGetById_andDelete() {
		TransactionEntity entity = new TransactionEntity();
		entity.setId("t1");
		entity.setType("expense");
		entity.setTxnDate(LocalDate.of(2026, 3, 1));
		entity.setMonthKey("2026-03");
		entity.setAmount(10L);
		entity.setLabel("Fuel");

		when(transactionRepository.findByDeletedAtIsNullOrderByTxnDateDesc()).thenReturn(List.of(entity));
		when(transactionRepository.findByIdAndDeletedAtIsNull("t1")).thenReturn(Optional.of(entity));
		when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		assertEquals(1, transactionService.listAll().size());
		assertEquals("t1", transactionService.getById("t1").id());

		transactionService.delete("t1");
		assertNotNull(entity.getDeletedAt());
	}

	/** Rewrites amount, month key, and label from an update request. */
	@Test
	void update_rewritesFields() {
		TransactionEntity entity = new TransactionEntity();
		entity.setId("t1");
		entity.setType("expense");
		when(transactionRepository.findByIdAndDeletedAtIsNull("t1")).thenReturn(Optional.of(entity));
		when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		TransactionRequest request = new TransactionRequest(
				"expense", LocalDate.of(2026, 4, 1), 20_000L, "v1", null, null,
				"bus", "fuel", "Diesel", null, null, null);

		var updated = transactionService.update("t1", request);
		assertEquals(20_000L, updated.amount());
		assertEquals("2026-04", updated.monthKey());
		assertEquals("Diesel", updated.label());
	}

	/** Throws when getById targets a missing or deleted transaction. */
	@Test
	void getById_notFound() {
		when(transactionRepository.findByIdAndDeletedAtIsNull("x")).thenReturn(Optional.empty());
		assertThrows(BusinessException.class, () -> transactionService.getById("x"));
	}

	private static TransactionRequest expenseOther(String label, String notes) {
		return new TransactionRequest(
				"expense",
				LocalDate.of(2026, 3, 12),
				50_000L,
				null,
				null,
				null,
				"bus",
				"other",
				label,
				null,
				null,
				notes);
	}

	private static TripEntity trip(String id, String vehicleId, String driverId, LocalDate dep, LocalDate ret) {
		TripEntity trip = new TripEntity();
		trip.setId(id);
		trip.setVehicleId(vehicleId);
		trip.setDriverId(driverId);
		trip.setDepartureDate(dep);
		trip.setReturnDate(ret);
		return trip;
	}
}
