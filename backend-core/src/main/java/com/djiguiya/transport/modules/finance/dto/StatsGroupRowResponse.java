package com.djiguiya.transport.modules.finance.dto;

/** Expense total for a category or category+subcategory pair in grouped stats. */
public record StatsGroupRowResponse(
		String categoryCode,
		String subcategoryCode,
		long amount
) {
}
