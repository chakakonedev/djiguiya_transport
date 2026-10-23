package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.ProfitShareRuleKinds;
import com.djiguiya.transport.modules.finance.dto.ProfitShareRuleUpdateRequest;
import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;
import com.djiguiya.transport.modules.finance.repository.ProfitShareRuleRepository;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.repository.StaffRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitShareRuleServiceTest {

	@Mock
	private ProfitShareRuleRepository profitShareRuleRepository;
	@Mock
	private StaffRepository staffRepository;
	@InjectMocks
	private ProfitShareRuleService profitShareRuleService;

	@Test
	void update_changesRateAndResolvesBeneficiaryName() {
		ProfitShareRuleEntity rule = new ProfitShareRuleEntity();
		rule.setId("rule-1");
		rule.setBeneficiaryStaffId("staff-1");
		rule.setRuleKind(ProfitShareRuleKinds.PARTNER_PROFIT_SHARE_PCT);
		rule.setRatePercent(new BigDecimal("1.0000"));
		rule.setEnabled(true);

		StaffEntity staff = new StaffEntity();
		staff.setId("staff-1");
		staff.setFullName("Diakaridia Koné");

		when(profitShareRuleRepository.findById("rule-1")).thenReturn(Optional.of(rule));
		when(profitShareRuleRepository.save(any(ProfitShareRuleEntity.class))).thenAnswer(inv -> inv.getArgument(0));
		when(staffRepository.findById("staff-1")).thenReturn(Optional.of(staff));

		var updated = profitShareRuleService.update(
				"rule-1",
				new ProfitShareRuleUpdateRequest(new BigDecimal("2.5000"), null, true, null));

		assertEquals(new BigDecimal("2.5000"), updated.ratePercent());
		assertEquals("Diakaridia Koné", updated.beneficiaryName());
	}

	@Test
	void listAll_includesNames() {
		ProfitShareRuleEntity rule = new ProfitShareRuleEntity();
		rule.setId("rule-1");
		rule.setBeneficiaryStaffId("staff-1");
		rule.setRuleKind(ProfitShareRuleKinds.QUARTERLY_SALARY_BONUS);
		rule.setSalaryMonthsEquivalent(new BigDecimal("1.0000"));
		rule.setEnabled(true);

		StaffEntity staff = new StaffEntity();
		staff.setId("staff-1");
		staff.setFullName("Tièdiè");

		when(profitShareRuleRepository.findAll()).thenReturn(List.of(rule));
		when(staffRepository.findAllById(List.of("staff-1"))).thenReturn(List.of(staff));

		var list = profitShareRuleService.listAll();
		assertEquals(1, list.size());
		assertEquals("Tièdiè", list.getFirst().beneficiaryName());
		assertEquals(new BigDecimal("1.0000"), list.getFirst().salaryMonthsEquivalent());
	}

	@Test
	void update_clearsBlankNotesAndDisables() {
		ProfitShareRuleEntity rule = new ProfitShareRuleEntity();
		rule.setId("rule-1");
		rule.setBeneficiaryStaffId(null);
		rule.setNotes("keep");
		rule.setEnabled(true);

		when(profitShareRuleRepository.findById("rule-1")).thenReturn(Optional.of(rule));
		when(profitShareRuleRepository.save(any(ProfitShareRuleEntity.class))).thenAnswer(inv -> inv.getArgument(0));

		var updated = profitShareRuleService.update(
				"rule-1",
				new ProfitShareRuleUpdateRequest(null, new BigDecimal("2.0"), false, "   "));

		assertEquals(false, updated.enabled());
		assertEquals(new BigDecimal("2.0"), updated.salaryMonthsEquivalent());
		assertEquals(null, updated.notes());
		assertEquals(null, updated.beneficiaryName());
	}

	@Test
	void update_notFound() {
		when(profitShareRuleRepository.findById("missing")).thenReturn(Optional.empty());
		org.junit.jupiter.api.Assertions.assertThrows(
				com.djiguiya.transport.shared.exception.BusinessException.class,
				() -> profitShareRuleService.update(
						"missing",
						new ProfitShareRuleUpdateRequest(null, null, null, null)));
	}

	@Test
	void listAll_nullBeneficiaryNameWhenStaffMissing() {
		ProfitShareRuleEntity rule = new ProfitShareRuleEntity();
		rule.setId("rule-1");
		rule.setBeneficiaryStaffId("gone");
		rule.setRuleKind(ProfitShareRuleKinds.PARTNER_PROFIT_SHARE_PCT);
		rule.setEnabled(true);

		when(profitShareRuleRepository.findAll()).thenReturn(List.of(rule));
		when(staffRepository.findAllById(List.of("gone"))).thenReturn(List.of());

		var list = profitShareRuleService.listAll();
		assertEquals(null, list.getFirst().beneficiaryName());
	}
}
