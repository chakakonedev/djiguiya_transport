package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.dto.ProfitShareRuleResponse;
import com.djiguiya.transport.modules.finance.dto.ProfitShareRuleUpdateRequest;
import com.djiguiya.transport.modules.finance.entities.ProfitShareRuleEntity;
import com.djiguiya.transport.modules.finance.repository.ProfitShareRuleRepository;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.repository.StaffRepository;
import com.djiguiya.transport.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfitShareRuleService {

	private final ProfitShareRuleRepository profitShareRuleRepository;
	private final StaffRepository staffRepository;

	@Transactional(readOnly = true)
	public List<ProfitShareRuleResponse> listAll() {
		List<ProfitShareRuleEntity> rules = profitShareRuleRepository.findAll();
		Map<String, StaffEntity> staffById = staffRepository.findAllById(
						rules.stream()
								.map(ProfitShareRuleEntity::getBeneficiaryStaffId)
								.filter(Objects::nonNull)
								.distinct()
								.toList())
				.stream()
				.collect(Collectors.toMap(StaffEntity::getId, Function.identity()));

		return rules.stream()
				.map(rule -> {
					StaffEntity staff = staffById.get(rule.getBeneficiaryStaffId());
					return ProfitShareRuleResponse.from(rule, staff != null ? staff.getFullName() : null);
				})
				.toList();
	}

	@Transactional
	public ProfitShareRuleResponse update(String id, ProfitShareRuleUpdateRequest request) {
		ProfitShareRuleEntity rule = profitShareRuleRepository.findById(id)
				.orElseThrow(() -> new BusinessException("profit_share.rule_not_found", HttpStatus.NOT_FOUND));

		if (request.ratePercent() != null) {
			rule.setRatePercent(request.ratePercent());
		}
		if (request.salaryMonthsEquivalent() != null) {
			rule.setSalaryMonthsEquivalent(request.salaryMonthsEquivalent());
		}
		if (request.enabled() != null) {
			rule.setEnabled(request.enabled());
		}
		if (request.notes() != null) {
			rule.setNotes(request.notes().isBlank() ? null : request.notes().trim());
		}

		ProfitShareRuleEntity saved = profitShareRuleRepository.save(rule);
		String name = null;
		if (saved.getBeneficiaryStaffId() != null) {
			name = staffRepository.findById(saved.getBeneficiaryStaffId())
					.map(StaffEntity::getFullName)
					.orElse(null);
		}
		return ProfitShareRuleResponse.from(saved, name);
	}
}
