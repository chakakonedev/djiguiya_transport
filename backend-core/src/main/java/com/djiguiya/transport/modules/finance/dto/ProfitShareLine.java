package com.djiguiya.transport.modules.finance.dto;

import java.math.BigDecimal;

public record ProfitShareLine(
		String ruleKind,
		String beneficiaryStaffId,
		String beneficiaryName,
		BigDecimal ratePercent,
		long amount
) {
}
