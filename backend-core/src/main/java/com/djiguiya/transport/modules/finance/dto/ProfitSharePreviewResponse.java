package com.djiguiya.transport.modules.finance.dto;

import java.util.List;

public record ProfitSharePreviewResponse(
		int year,
		String periodStart,
		String periodEnd,
		long totalRevenue,
		long totalExpense,
		long annualNetProfit,
		long reserveAmount,
		long distributableAmount,
		List<ProfitShareLine> lines
) {
}
