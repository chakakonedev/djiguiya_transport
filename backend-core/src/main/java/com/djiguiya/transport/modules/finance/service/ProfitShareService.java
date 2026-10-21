package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.ProfitShareRuleKinds;
import com.djiguiya.transport.modules.finance.dto.ProfitShareLine;
import com.djiguiya.transport.modules.finance.dto.ProfitSharePreviewResponse;
import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.ProfitShareRuleRepository;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfitShareService {

	private final TransactionRepository transactionRepository;
	private final ProfitShareRuleRepository profitShareRuleRepository;

	@Transactional(readOnly = true)
	public ProfitSharePreviewResponse preview(int year) {
		List<TransactionEntity> ledger = transactionRepository.findByYearPrefix(String.valueOf(year), null, null);
		long revenue = LedgerAggregator.totalRevenue(ledger);
		long expense = LedgerAggregator.totalExpense(ledger);
		long netProfit = revenue - expense;

		LocalDate onDate = LocalDate.of(year, 12, 31);
		List<ProfitShareRuleEntity> rules = profitShareRuleRepository.findActiveOnDate(onDate);

		BigDecimal reserveRate = rules.stream()
				.filter(r -> ProfitShareRuleKinds.COMPANY_GROWTH_RESERVE_PCT.equals(r.getRuleKind()))
				.map(ProfitShareRuleEntity::getRatePercent)
				.findFirst()
				.orElse(BigDecimal.ZERO);

		long reserveAmount = percentOf(netProfit, reserveRate);
		long distributable = Math.max(0, netProfit - reserveAmount);

		List<ProfitShareLine> partnerShares = new ArrayList<>();
		for (ProfitShareRuleEntity rule : rules) {
			if (!ProfitShareRuleKinds.PARTNER_PROFIT_SHARE_PCT.equals(rule.getRuleKind())) {
				continue;
			}
			long share = percentOf(distributable, rule.getRatePercent());
			partnerShares.add(new ProfitShareLine(
					rule.getRuleKind(),
					rule.getBeneficiaryStaffId(),
					rule.getRatePercent(),
					share));
		}

		return new ProfitSharePreviewResponse(
				year,
				revenue,
				expense,
				netProfit,
				reserveAmount,
				distributable,
				partnerShares);
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
