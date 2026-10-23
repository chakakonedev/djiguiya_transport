package com.djiguiya.transport.modules.finance.dto;

/** One calendar month within a fiscal-year stats series. */
public record StatsMonthPointResponse(
		String monthKey,
		int calendarMonth,
		int calendarYear,
		long totalRevenue,
		long totalExpense,
		long netProfit,
		int tripCount
) {
}
