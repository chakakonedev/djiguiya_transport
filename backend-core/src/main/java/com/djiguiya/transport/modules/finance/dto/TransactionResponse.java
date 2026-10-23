package com.djiguiya.transport.modules.finance.dto;

import com.djiguiya.transport.modules.finance.entities.TransactionEntity;

import java.time.LocalDate;

/** API view of a persisted ledger row. */
public record TransactionResponse(
		String id,
		String type,
		LocalDate txnDate,
		String monthKey,
		Long amount,
		String vehicleId,
		String driverId,
		String tripId,
		String categoryCode,
		String subcategoryCode,
		String label,
		String payrollMonth,
		String payrollKind,
		String notes
) {
	public static TransactionResponse from(TransactionEntity e) {
		return new TransactionResponse(
				e.getId(),
				e.getType(),
				e.getTxnDate(),
				e.getMonthKey(),
				e.getAmount(),
				e.getVehicleId(),
				e.getDriverId(),
				e.getTripId(),
				e.getCategoryCode(),
				e.getSubcategoryCode(),
				e.getLabel(),
				e.getPayrollMonth(),
				e.getPayrollKind(),
				e.getNotes());
	}
}
