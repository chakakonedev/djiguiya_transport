package com.djiguiya.transport.modules.finance.service;

import com.djiguiya.transport.modules.finance.domain.TransactionTypes;
import com.djiguiya.transport.modules.finance.dto.PayrollPaymentRequest;
import com.djiguiya.transport.modules.finance.dto.TransactionRequest;
import com.djiguiya.transport.modules.finance.dto.TransactionResponse;
import com.djiguiya.transport.modules.staff.entities.StaffEntity;
import com.djiguiya.transport.modules.staff.service.StaffService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PayrollService}: salary/bonus posts delegate to {@link TransactionService} with fixed codes.
 */
@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

	@Mock
	private TransactionService transactionService;
	@Mock
	private StaffService staffService;
	@InjectMocks
	private PayrollService payrollService;

	/** Builds a payroll expense with default label and salary subcategory. */
	@Test
	void postSalary_createsExpenseWithDefaults() {
		when(staffService.findEntity("s1")).thenReturn(staff("s1"));
		when(transactionService.create(any())).thenReturn(response("salary"));

		PayrollPaymentRequest request = new PayrollPaymentRequest(
				"s1", 150_000L, "2026-03", LocalDate.of(2026, 3, 28), null, null);

		payrollService.postSalary(request);

		ArgumentCaptor<TransactionRequest> captor = ArgumentCaptor.forClass(TransactionRequest.class);
		verify(transactionService).create(captor.capture());
		TransactionRequest txn = captor.getValue();
		assertEquals(TransactionTypes.EXPENSE, txn.type());
		assertEquals("s1", txn.driverId());
		assertEquals("payroll", txn.categoryCode());
		assertEquals("salary", txn.subcategoryCode());
		assertEquals("salary", txn.payrollKind());
		assertEquals("2026-03", txn.payrollMonth());
		assertEquals("Salary payment", txn.label());
		assertNull(txn.notes());
	}

	/** Passes custom label and notes through for bonus payments. */
	@Test
	void postBonus_usesCustomLabel() {
		when(staffService.findEntity("s1")).thenReturn(staff("s1"));
		when(transactionService.create(any())).thenReturn(response("bonus"));

		PayrollPaymentRequest request = new PayrollPaymentRequest(
				"s1", 50_000L, "2026-03", LocalDate.of(2026, 3, 28), "Prime spéciale", "notes");

		payrollService.postBonus(request);

		ArgumentCaptor<TransactionRequest> captor = ArgumentCaptor.forClass(TransactionRequest.class);
		verify(transactionService).create(captor.capture());
		assertEquals("bonus", captor.getValue().subcategoryCode());
		assertEquals("Prime spéciale", captor.getValue().label());
		assertEquals("notes", captor.getValue().notes());
	}

	private static StaffEntity staff(String id) {
		StaffEntity entity = new StaffEntity();
		entity.setId(id);
		entity.setFullName("Driver");
		return entity;
	}

	private static TransactionResponse response(String kind) {
		return new TransactionResponse(
				"t1",
				"expense",
				LocalDate.of(2026, 3, 28),
				"2026-03",
				100L,
				null,
				"s1",
				null,
				"payroll",
				kind,
				"label",
				"2026-03",
				kind,
				null);
	}
}
