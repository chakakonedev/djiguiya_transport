package com.djiguiya.transport.modules.finance.dto;

import java.util.List;

public record ProfitSharePreviewResponse(
		int year,
		long totalRevenue,
		long totalExpense,
		long netProfit,
		long companyReserveAmount,
		long distributableProfit,
		List<ProfitShareLine> partnerShares
) {
}
