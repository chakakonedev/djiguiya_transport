package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

	@Mock
	private TransactionRepository transactionRepository;
	@InjectMocks
	private StatsService statsService;

	@Test
	void summary_computesTotals() {
		TransactionEntity revenue = txn(TransactionTypes.REVENUE, 1000L, "trip-1");
		TransactionEntity expense = txn(TransactionTypes.EXPENSE, 400L, null);
		when(transactionRepository.findFiltered("2026-03", "v1", null, null, null))
				.thenReturn(List.of(revenue, expense));

		var summary = statsService.summary("v1", null, 3, 2026, null);
		assertEquals(1000L, summary.totalRevenue());
		assertEquals(400L, summary.totalExpense());
		assertEquals(600L, summary.netProfit());
		assertEquals(1, summary.tripCount());
		assertEquals(2, summary.transactionCount());
	}

	private static TransactionEntity txn(String type, long amount, String tripId) {
		TransactionEntity e = new TransactionEntity();
		e.setType(type);
		e.setAmount(amount);
		e.setTripId(tripId);
		return e;
	}
}
