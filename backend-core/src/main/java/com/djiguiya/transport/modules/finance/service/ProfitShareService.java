package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.ProfitShareRuleKinds;
import com.djiguiya.transport.modules.finance.dto.ProfitShareLine;
import com.djiguiya.transport.modules.finance.dto.ProfitSharePreviewResponse;
import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.ProfitShareRuleRepository;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.repository.StaffRepository;
import com.djiguiya.transport.shared.util.FiscalYearUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfitShareService {

	private final TransactionRepository transactionRepository;
	private final ProfitShareRuleRepository profitShareRuleRepository;
	private final StaffRepository staffRepository;

	@Transactional(readOnly = true)
	public ProfitSharePreviewResponse preview(int fiscalStartYear) {
		String fromMonth = FiscalYearUtil.startMonthKey(fiscalStartYear);
		String toMonth = FiscalYearUtil.endMonthKey(fiscalStartYear);
		List<TransactionEntity> ledger = transactionRepository.findByMonthKeyRange(fromMonth, toMonth, null, null);
		long revenue = LedgerAggregator.totalRevenue(ledger);
		long expense = LedgerAggregator.totalExpense(ledger);
		long netProfit = revenue - expense;

		List<ProfitShareRuleEntity> rules = profitShareRuleRepository.findActiveOnDate(
				FiscalYearUtil.endDate(fiscalStartYear));

		BigDecimal reserveRate = rules.stream()
				.filter(r -> ProfitShareRuleKinds.COMPANY_GROWTH_RESERVE_PCT.equals(r.getRuleKind()))
				.map(ProfitShareRuleEntity::getRatePercent)
				.findFirst()
				.orElse(BigDecimal.ZERO);

		long reserveAmount = percentOf(netProfit, reserveRate);
		long distributable = Math.max(0, netProfit - reserveAmount);

		Map<String, StaffEntity> staffById = staffRepository.findAllById(
						rules.stream()
								.map(ProfitShareRuleEntity::getBeneficiaryStaffId)
								.filter(Objects::nonNull)
								.distinct()
								.toList())
				.stream()
				.collect(Collectors.toMap(StaffEntity::getId, Function.identity()));

		List<ProfitShareLine> lines = new ArrayList<>();
		for (ProfitShareRuleEntity rule : rules) {
			if (!ProfitShareRuleKinds.PARTNER_PROFIT_SHARE_PCT.equals(rule.getRuleKind())) {
				continue;
			}
			long share = percentOf(distributable, rule.getRatePercent());
			StaffEntity staff = staffById.get(rule.getBeneficiaryStaffId());
			lines.add(new ProfitShareLine(
					rule.getRuleKind(),
					rule.getBeneficiaryStaffId(),
					staff != null ? staff.getFullName() : null,
					rule.getRatePercent(),
					share));
		}

		return new ProfitSharePreviewResponse(
				fiscalStartYear,
				fromMonth,
				toMonth,
				revenue,
				expense,
				netProfit,
				reserveAmount,
				distributable,
				lines);
	}

	private static long percentOf(long base, BigDecimal ratePercent) {
		if (ratePercent == null || base <= 0) {
			return 0L;
		}
		BigDecimal amount = BigDecimal.valueOf(base)
				.multiply(ratePercent)
				.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
		return amount.longValue();
	}
}
