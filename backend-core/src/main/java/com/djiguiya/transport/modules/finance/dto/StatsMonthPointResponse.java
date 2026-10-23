package com.djiguiya.transport.modules.finance.dto;

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
