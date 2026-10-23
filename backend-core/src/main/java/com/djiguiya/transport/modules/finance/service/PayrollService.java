package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.dto.PayrollPaymentRequest;
import com.djiguiya.transport.modules.finance.dto.TransactionResponse;
import com.djiguiya.transport.modules.staff.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Wraps payroll flows as expense transactions: category {@code payroll}, subcategory {@code salary}
 * or {@code bonus}, with {@code driverId} holding the staff id and optional {@code payrollMonth}.
 */
@Service
@RequiredArgsConstructor
public class PayrollService {

	private static final String CATEGORY_PAYROLL = "payroll";
	private static final String KIND_SALARY = "salary";
	private static final String KIND_BONUS = "bonus";

	private final TransactionService transactionService;
	private final StaffService staffService;

	/** Validates staff exists, then creates a salary expense ledger entry. */
	@Transactional
	public TransactionResponse postSalary(PayrollPaymentRequest request) {
		return postPayroll(request, KIND_SALARY, defaultLabel(request, "Salary payment"));
	}

	/** Same as salary but subcategory and payroll kind are {@code bonus}. */
	@Transactional
	public TransactionResponse postBonus(PayrollPaymentRequest request) {
		return postPayroll(request, KIND_BONUS, defaultLabel(request, "Bonus payment"));
	}

	private TransactionResponse postPayroll(PayrollPaymentRequest request, String kind, String defaultLabel) {
		staffService.findEntity(request.staffId());
		var txnRequest = new com.djiguiya.transport.modules.finance.dto.TransactionRequest(
				TransactionTypes.EXPENSE,
				request.txnDate(),
				request.amount(),
				null,
				request.staffId(),
				null,
				CATEGORY_PAYROLL,
				kind,
				request.label() != null ? request.label() : defaultLabel,
				request.payrollMonth(),
				kind,
				request.notes());
		return transactionService.create(txnRequest);
	}

	private String defaultLabel(PayrollPaymentRequest request, String fallback) {
		return request.label() != null && !request.label().isBlank() ? request.label() : fallback;
	}
}
