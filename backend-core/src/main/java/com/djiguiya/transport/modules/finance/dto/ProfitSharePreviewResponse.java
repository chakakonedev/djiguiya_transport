package com.djiguiya.transport.modules.finance.dto;

import java.util.List;

/** Fiscal-year profit-share simulation: totals, reserve, distributable pool, and partner lines. */
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
