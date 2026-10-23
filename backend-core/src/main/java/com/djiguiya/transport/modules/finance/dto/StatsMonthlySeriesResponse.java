package com.djiguiya.transport.modules.finance.dto;

import java.util.List;

public record StatsMonthlySeriesResponse(
		int fiscalStartYear,
		List<StatsMonthPointResponse> months
) {
}
