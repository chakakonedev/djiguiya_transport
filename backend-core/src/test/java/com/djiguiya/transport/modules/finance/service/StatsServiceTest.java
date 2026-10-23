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
		TransactionEntity revenue = txn(TransactionTypes.REVENUE, 1000L, "trip-1", null);
		TransactionEntity expense = txn(TransactionTypes.EXPENSE, 400L, null, "bus");
		when(transactionRepository.findFiltered("2026-03", "v1", null, null, null))
				.thenReturn(List.of(revenue, expense));

		var summary = statsService.summary("v1", null, 3, 2026, null);
		assertEquals(1000L, summary.totalRevenue());
		assertEquals(400L, summary.totalExpense());
		assertEquals(600L, summary.netProfit());
		assertEquals(1, summary.tripCount());
		assertEquals(2, summary.transactionCount());
		assertEquals(1, summary.groups().size());
		assertEquals("bus", summary.groups().getFirst().categoryCode());
		assertEquals(400L, summary.groups().getFirst().amount());
	}

	@Test
	void monthlySeries_coversFiscalYearAugToJul() {
		when(transactionRepository.findByMonthKeyRange("2025-08", "2026-07", null, null))
				.thenReturn(List.of());

		var series = statsService.monthlySeries(2025, null, null);
		assertEquals(2025, series.fiscalStartYear());
		assertEquals(12, series.months().size());
		assertEquals("2025-08", series.months().getFirst().monthKey());
		assertEquals("2026-07", series.months().get(11).monthKey());
	}

	private static TransactionEntity txn(String type, long amount, String tripId, String category) {
		TransactionEntity e = new TransactionEntity();
		e.setType(type);
		e.setAmount(amount);
		e.setTripId(tripId);
		e.setCategoryCode(category);
		return e;
	}
}
