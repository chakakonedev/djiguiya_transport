package com.djiguiya.transport.modules.finance.dto;

import java.util.List;

/** Twelve fiscal-year months of revenue, expense, net, and trip counts. */
public record StatsMonthlySeriesResponse(
		int fiscalStartYear,
		List<StatsMonthPointResponse> months
) {
}
