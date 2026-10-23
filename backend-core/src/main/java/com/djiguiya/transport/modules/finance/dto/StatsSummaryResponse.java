package com.djiguiya.transport.modules.finance.dto;

public record StatsSummaryResponse(
		String monthKey,
		Integer year,
		String vehicleId,
		String driverId,
		String categoryCode,
		long totalRevenue,
		long totalExpense,
		long netProfit,
		int tripCount,
		int transactionCount
) {
}
