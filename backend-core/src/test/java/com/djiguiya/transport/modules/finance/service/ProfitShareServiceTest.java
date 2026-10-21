package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.ProfitShareRuleKinds;
import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.ProfitShareRuleRepository;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitShareServiceTest {

	@Mock
	private TransactionRepository transactionRepository;
	@Mock
	private ProfitShareRuleRepository profitShareRuleRepository;
	@InjectMocks
	private ProfitShareService profitShareService;

	@Test
	void preview_appliesReserveRule() {
		TransactionEntity revenue = new TransactionEntity();
		revenue.setType(TransactionTypes.REVENUE);
		revenue.setAmount(1000L);
		TransactionEntity expense = new TransactionEntity();
		expense.setType(TransactionTypes.EXPENSE);
		expense.setAmount(200L);

		ProfitShareRuleEntity reserve = new ProfitShareRuleEntity();
		reserve.setRuleKind(ProfitShareRuleKinds.COMPANY_GROWTH_RESERVE_PCT);
		reserve.setRatePercent(new BigDecimal("20.0000"));

		when(transactionRepository.findByYearPrefix("2026", null, null)).thenReturn(List.of(revenue, expense));
		when(profitShareRuleRepository.findActiveOnDate(any(LocalDate.class))).thenReturn(List.of(reserve));

		var preview = profitShareService.preview(2026);
		assertEquals(800L, preview.netProfit());
		assertEquals(160L, preview.companyReserveAmount());
		assertEquals(640L, preview.distributableProfit());
	}
}
