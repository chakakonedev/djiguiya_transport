package com.djiguiya.transport.modules.finance.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "profit_share_rules")
@Getter
@Setter
public class ProfitShareRuleEntity {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "beneficiary_staff_id", length = 36)
	private String beneficiaryStaffId;

	@Column(name = "rule_kind", nullable = false, length = 40)
	private String ruleKind;

	@Column(name = "rate_percent", precision = 8, scale = 4)
	private BigDecimal ratePercent;

	@Column(name = "salary_months_equivalent", precision = 8, scale = 4)
	private BigDecimal salaryMonthsEquivalent;

	@Column(nullable = false)
	private boolean enabled = true;

	@Column(name = "effective_from", nullable = false)
	private LocalDate effectiveFrom;

	@Column(name = "effective_to")
	private LocalDate effectiveTo;

	@Column(columnDefinition = "TEXT")
	private String notes;
}
