package com.djiguiya.transport.config;

import com.djiguiya.transport.modules.finance.domain.ProfitShareRuleKinds;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Canonical core personnel always present at application startup.
 */
public final class CoreStaffDefinitions {

	public static final LocalDate EFFECTIVE_FROM = LocalDate.of(2025, 8, 1);

	public static final List<Person> PEOPLE = List.of(
			new Person(
					"00000000-0000-0000-0000-000000000001",
					"00000000-0000-0000-0000-000000000101",
					"chakakone93@yahoo.fr",
					"Chaka Koné",
					"owner",
					null,
					null,
					"CEO / PDG — no salary"
			),
			new Person(
					"00000000-0000-0000-0000-000000000002",
					"00000000-0000-0000-0000-000000000102",
					"tiedie.kone@djiguiya.ml",
					"Tièdiè Tièkoroba Koné",
					"driver_manager",
					50_000L,
					28,
					"Driver manager — monthly salary + revenue/profit shares"
			),
			new Person(
					"00000000-0000-0000-0000-000000000003",
					"00000000-0000-0000-0000-000000000103",
					"diakaridia.kone@djiguiya.ml",
					"Diakaridia Koné",
					"accountant",
					null,
					null,
					"Accountant — 1% annual net profit share"
			),
			new Person(
					"00000000-0000-0000-0000-000000000004",
					"00000000-0000-0000-0000-000000000104",
					"sekou.kone@djiguiya.ml",
					"Sekou Koné",
					"cashier_accountant",
					null,
					null,
					"Cashier-accountant — 1% annual net profit share"
			)
	);

	public static final List<ShareRule> SHARE_RULES = List.of(
			new ShareRule(
					"00000000-0000-0000-0000-000000000202",
					"00000000-0000-0000-0000-000000000102",
					ProfitShareRuleKinds.QUARTERLY_REVENUE_SHARE_PCT,
					new BigDecimal("1.0000"),
					"1% of quarterly revenue (recettes)"
			),
			new ShareRule(
					"00000000-0000-0000-0000-000000000203",
					"00000000-0000-0000-0000-000000000102",
					ProfitShareRuleKinds.PARTNER_PROFIT_SHARE_PCT,
					new BigDecimal("1.0000"),
					"1% of annual net profit — payable on August 1"
			),
			new ShareRule(
					"00000000-0000-0000-0000-000000000204",
					"00000000-0000-0000-0000-000000000103",
					ProfitShareRuleKinds.PARTNER_PROFIT_SHARE_PCT,
					new BigDecimal("1.0000"),
					"1% of annual net profit"
			),
			new ShareRule(
					"00000000-0000-0000-0000-000000000205",
					"00000000-0000-0000-0000-000000000104",
					ProfitShareRuleKinds.PARTNER_PROFIT_SHARE_PCT,
					new BigDecimal("1.0000"),
					"1% of annual net profit"
			)
	);

	private CoreStaffDefinitions() {
	}

	public record Person(
			String userId,
			String staffId,
			String email,
			String fullName,
			String roleCode,
			Long monthlySalary,
			Integer payDay,
			String notes
	) {
	}

	public record ShareRule(
			String id,
			String beneficiaryStaffId,
			String ruleKind,
			BigDecimal ratePercent,
			String notes
	) {
	}
}
