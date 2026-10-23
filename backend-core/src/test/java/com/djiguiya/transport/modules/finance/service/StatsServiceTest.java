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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

	@Mock
	private TransactionRepository transactionRepository;
	@InjectMocks
	private StatsService statsService;

	@Test
	void summary_computesTotals() {
		TransactionEntity revenue = txn(TransactionTypes.REVENUE, 1000L, "trip-1", null, "2026-03");
		TransactionEntity expense = txn(TransactionTypes.EXPENSE, 400L, null, "bus", "2026-03");
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
	void summary_withoutMonthUsesAllFiltered() {
		when(transactionRepository.findFiltered(null, null, null, "bus", null))
				.thenReturn(List.of(txn(TransactionTypes.EXPENSE, 100L, null, "bus", "2026-01")));

		var summary = statsService.summary(null, null, null, null, "bus");
		assertNull(summary.monthKey());
		assertEquals(100L, summary.totalExpense());
	}

	@Test
	void summary_fiscalYearFiltersCategory() {
		TransactionEntity match = txn(TransactionTypes.EXPENSE, 50L, null, "bus", "2025-09");
		TransactionEntity other = txn(TransactionTypes.EXPENSE, 20L, null, "family", "2025-09");
		when(transactionRepository.findByMonthKeyRange("2025-08", "2026-07", null, null))
				.thenReturn(List.of(match, other));

		var summary = statsService.summary(null, null, null, 2025, "bus");
		assertEquals(50L, summary.totalExpense());
		assertEquals(1, summary.transactionCount());
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

	@Test
	void compareMonth_computesDelta() {
		when(transactionRepository.findFiltered(any(), isNull(), isNull(), isNull(), isNull()))
				.thenReturn(List.of(txn(TransactionTypes.REVENUE, 200L, "t1", null, "x")))
				.thenReturn(List.of(txn(TransactionTypes.REVENUE, 50L, "t2", null, "y")));

		var compare = statsService.compareMonth(3, null, null);
		assertEquals(200L, compare.currentNetProfit());
		assertEquals(50L, compare.previousNetProfit());
		assertEquals(150L, compare.deltaNetProfit());
	}

	@Test
	void yearOverYear_handlesZeroPrevious() {
		when(transactionRepository.findByMonthKeyRange(eq("2025-08"), eq("2026-07"), isNull(), isNull()))
				.thenReturn(List.of(txn(TransactionTypes.REVENUE, 100L, "t1", null, "2025-08")));
		when(transactionRepository.findByMonthKeyRange(eq("2024-08"), eq("2025-07"), isNull(), isNull()))
				.thenReturn(List.of());

		var yoy = statsService.yearOverYear(2025, null, null);
		assertEquals(100L, yoy.currentNetProfit());
		assertEquals(0L, yoy.previousNetProfit());
		assertNull(yoy.percentChange());
	}

	@Test
	void yearOverYear_computesPercent() {
		when(transactionRepository.findByMonthKeyRange(eq("2025-08"), eq("2026-07"), isNull(), isNull()))
				.thenReturn(List.of(txn(TransactionTypes.REVENUE, 150L, "t1", null, "2025-08")));
		when(transactionRepository.findByMonthKeyRange(eq("2024-08"), eq("2025-07"), isNull(), isNull()))
				.thenReturn(List.of(txn(TransactionTypes.REVENUE, 100L, "t2", null, "2024-08")));

		var yoy = statsService.yearOverYear(2025, null, null);
		assertEquals(50.0, yoy.percentChange());
	}

	private static TransactionEntity txn(String type, long amount, String tripId, String category, String monthKey) {
		TransactionEntity e = new TransactionEntity();
		e.setType(type);
		e.setAmount(amount);
		e.setTripId(tripId);
		e.setCategoryCode(category);
		e.setMonthKey(monthKey);
		return e;
	}
}
