package com.djiguiya.transport.modules.finance.dto;

public record StatsGroupRowResponse(
		String categoryCode,
		String subcategoryCode,
		long amount
) {
}
