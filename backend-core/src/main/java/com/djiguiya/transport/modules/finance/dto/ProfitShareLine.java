package com.djiguiya.transport.modules.finance.dto;

import java.math.BigDecimal;

/** One beneficiary's computed share in a profit-share preview. */
public record ProfitShareLine(
		String ruleKind,
		String beneficiaryStaffId,
		String beneficiaryName,
		BigDecimal ratePercent,
		long amount
) {
}
