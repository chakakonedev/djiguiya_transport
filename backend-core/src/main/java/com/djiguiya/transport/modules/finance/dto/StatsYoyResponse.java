package com.djiguiya.transport.modules.finance.dto;

/** Fiscal year-over-year net profit comparison; {@code percentChange} may be null. */
public record StatsYoyResponse(
		int year,
		int previousYear,
		long currentNetProfit,
		long previousNetProfit,
		long deltaNetProfit,
		Double percentChange
) {
}
