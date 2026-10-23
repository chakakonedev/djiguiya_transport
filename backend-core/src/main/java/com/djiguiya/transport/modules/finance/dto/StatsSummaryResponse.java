package com.djiguiya.transport.modules.finance.dto;

import java.util.List;

/** Aggregated stats for a filter scope, including expense breakdown in {@code groups}. */
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
		int transactionCount,
		List<StatsGroupRowResponse> groups
) {
}
