package com.djiguiya.transport.modules.finance.dto;

import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Profit-share rule with optional resolved beneficiary name for the UI. */
public record ProfitShareRuleResponse(
		String id,
		String beneficiaryStaffId,
		String beneficiaryName,
		String ruleKind,
		BigDecimal ratePercent,
		BigDecimal salaryMonthsEquivalent,
		boolean enabled,
		LocalDate effectiveFrom,
		LocalDate effectiveTo,
		String notes
) {
	public static ProfitShareRuleResponse from(ProfitShareRuleEntity e, String beneficiaryName) {
		return new ProfitShareRuleResponse(
				e.getId(),
				e.getBeneficiaryStaffId(),
				beneficiaryName,
				e.getRuleKind(),
				e.getRatePercent(),
				e.getSalaryMonthsEquivalent(),
				e.isEnabled(),
				e.getEffectiveFrom(),
				e.getEffectiveTo(),
				e.getNotes());
	}
}
