package com.djiguiya.transport.modules.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record ProfitShareRuleUpdateRequest(
		@Digits(integer = 4, fraction = 4)
		@DecimalMin("0.0")
		BigDecimal ratePercent,
		@Digits(integer = 4, fraction = 4)
		@DecimalMin("0.0")
		BigDecimal salaryMonthsEquivalent,
		Boolean enabled,
		String notes
) {
}
