package com.djiguiya.transport.modules.finance.dto;

public record StatsCompareMonthResponse(
		int monthIndex,
		int year,
		String currentMonthKey,
		String previousMonthKey,
		long currentNetProfit,
		long previousNetProfit,
		long deltaNetProfit
) {
}
