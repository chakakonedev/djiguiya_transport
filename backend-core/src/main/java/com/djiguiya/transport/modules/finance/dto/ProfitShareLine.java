package com.djiguiya.transport.modules.finance.dto;

import java.math.BigDecimal;

public record ProfitShareLine(
		String ruleKind,
		String beneficiaryStaffId,
		BigDecimal ratePercent,
		long amount
) {
}
