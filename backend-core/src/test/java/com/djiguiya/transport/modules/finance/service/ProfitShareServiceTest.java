package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.ProfitShareRuleKinds;
import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;
import com.djiguiya.transport.modules.finance.entities.TransactionEntity;
import com.djiguiya.transport.modules.finance.repository.ProfitShareRuleRepository;
import com.djiguiya.transport.modules.finance.repository.TransactionRepository;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.repository.StaffRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitShareServiceTest {

	@Mock
	private TransactionRepository transactionRepository;
	@Mock
	private ProfitShareRuleRepository profitShareRuleRepository;
	@Mock
	private StaffRepository staffRepository;
	@InjectMocks
	private ProfitShareService profitShareService;

	@Test
	void preview_usesFiscalYearRangeAndAppliesReserveRule() {
		TransactionEntity revenue = new TransactionEntity();
		revenue.setType(TransactionTypes.REVENUE);
		revenue.setAmount(1000L);
		TransactionEntity expense = new TransactionEntity();
		expense.setType(TransactionTypes.EXPENSE);
		expense.setAmount(200L);

		ProfitShareRuleEntity reserve = new ProfitShareRuleEntity();
		reserve.setRuleKind(ProfitShareRuleKinds.COMPANY_GROWTH_RESERVE_PCT);
		reserve.setRatePercent(new BigDecimal("20.0000"));

		ProfitShareRuleEntity partner = new ProfitShareRuleEntity();
		partner.setRuleKind(ProfitShareRuleKinds.PARTNER_PROFIT_SHARE_PCT);
		partner.setBeneficiaryStaffId("staff-1");
		partner.setRatePercent(new BigDecimal("1.0000"));

		StaffEntity staff = new StaffEntity();
		staff.setId("staff-1");
		staff.setFullName("Diakaridia Koné");

		when(transactionRepository.findByMonthKeyRange("2025-08", "2026-07", null, null))
				.thenReturn(List.of(revenue, expense));
		when(profitShareRuleRepository.findActiveOnDate(any(LocalDate.class)))
				.thenReturn(List.of(reserve, partner));
		when(staffRepository.findAllById(eq(List.of("staff-1")))).thenReturn(List.of(staff));

		var preview = profitShareService.preview(2025);
		assertEquals("2025-08", preview.periodStart());
		assertEquals("2026-07", preview.periodEnd());
		assertEquals(800L, preview.annualNetProfit());
		assertEquals(160L, preview.reserveAmount());
		assertEquals(640L, preview.distributableAmount());
		assertEquals(1, preview.lines().size());
		assertEquals("Diakaridia Koné", preview.lines().getFirst().beneficiaryName());
		assertEquals(6L, preview.lines().getFirst().amount());
	}
}
