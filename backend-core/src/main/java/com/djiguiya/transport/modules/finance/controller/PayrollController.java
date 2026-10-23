package com.djiguiya.transport.modules.finance.controller;

import com.djiguiya.transport.modules.finance.dto.PayrollPaymentRequest;
import com.djiguiya.transport.modules.finance.dto.TransactionResponse;
import com.djiguiya.transport.modules.finance.service.PayrollService;
import com.djiguiya.transport.shared.api.ApiResponse;
import com.djiguiya.transport.shared.security.AppRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Posts payroll-related expenses as standard ledger transactions under category {@code payroll}.
 * Mutations require the same ledger-write roles as {@link TransactionController}.
 */
@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {

	private final PayrollService payrollService;

	/** Records a salary payment for a staff member for the given payroll month. */
	@PostMapping("/salary")
	@PreAuthorize(AppRoles.LEDGER_WRITE)
	public ApiResponse<TransactionResponse> salary(@Valid @RequestBody PayrollPaymentRequest request) {
		return ApiResponse.ok("transaction.created", payrollService.postSalary(request));
	}

	/** Records a bonus payment using the same payroll expense shape as salary. */
	@PostMapping("/bonus")
	@PreAuthorize(AppRoles.LEDGER_WRITE)
	public ApiResponse<TransactionResponse> bonus(@Valid @RequestBody PayrollPaymentRequest request) {
		return ApiResponse.ok("transaction.created", payrollService.postBonus(request));
	}
}
