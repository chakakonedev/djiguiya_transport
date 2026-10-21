package com.djiguiya.transport.modules.finance.dto;

public record StatsYoyResponse(
		int year,
		int previousYear,
		long currentNetProfit,
		long previousNetProfit,
		long deltaNetProfit,
		Double percentChange
) {
}
