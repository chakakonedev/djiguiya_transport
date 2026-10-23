package com.djiguiya.transport.modules.finance.dto;

/** Net profit for a calendar month versus the prior month in the current year. */
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
