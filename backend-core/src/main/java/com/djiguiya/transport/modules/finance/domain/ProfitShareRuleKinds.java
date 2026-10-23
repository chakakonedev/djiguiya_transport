package com.djiguiya.transport.modules.finance.domain;

/**
 * Stored {@code ruleKind} values for {@code profit_share_rules}. Reserve and partner kinds drive
 * the annual preview; quarterly salary bonus uses {@code salaryMonthsEquivalent} elsewhere.
 */
public final class ProfitShareRuleKinds {

	/** Percent of annual net profit retained before partner distribution. */
	public static final String COMPANY_GROWTH_RESERVE_PCT = "company_growth_reserve_pct";
	/** Percent of distributable profit paid to a beneficiary staff member. */
	public static final String PARTNER_PROFIT_SHARE_PCT = "partner_profit_share_pct";
	/** N × monthly salary paid each quarter (driver managers). */
	public static final String QUARTERLY_SALARY_BONUS = "quarterly_salary_bonus";

	private ProfitShareRuleKinds() {
	}
}
