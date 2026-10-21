package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.dto.TransactionRequest;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.modules.trips.service.TripService;
import com.djiguiya.transport.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

	@Mock
	private TransactionRepository transactionRepository;
	@Mock
	private TripService tripService;
	@InjectMocks
	private TransactionService transactionService;

	@Test
	void create_rejectsOtherSubcategoryWithoutLabelAndNotes() {
		TransactionRequest request = new TransactionRequest(
				"expense",
				LocalDate.of(2026, 3, 12),
				50_000L,
				null,
				null,
				null,
				"bus",
				"other",
				null,
				null,
				null,
				null);

		BusinessException ex = assertThrows(BusinessException.class, () -> transactionService.create(request));
		assertEquals("transaction.other_requires_details", ex.getMessageKey());
	}

	@Test
	void create_acceptsOtherSubcategoryWithLabelAndNotes() {
		TransactionRequest request = new TransactionRequest(
				"expense",
				LocalDate.of(2026, 3, 12),
				50_000L,
				null,
				null,
				null,
				"bus",
				"other",
				"Réparation spéciale",
				null,
				null,
				"Remplacement du câble électrique principal");

		when(transactionRepository.save(any(TransactionEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		transactionService.create(request);

		verify(transactionRepository).save(any(TransactionEntity.class));
	}
}
