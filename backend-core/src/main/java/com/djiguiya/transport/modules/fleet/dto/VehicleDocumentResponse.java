package com.djiguiya.transport.modules.fleet.dto;

import com.djiguiya.transport.modules.fleet.entities.VehicleDocumentEntity;

import java.time.LocalDate;

/** Compliance document state for a vehicle (insurance, registration, etc.). */
public record VehicleDocumentResponse(
		String id,
		String vehicleId,
		String docType,
		LocalDate expiresOn,
		LocalDate lastRenewedOn,
		int renewalPeriodMonths,
		Long lastAmount,
		String notes
) {
	public static VehicleDocumentResponse from(VehicleDocumentEntity e) {
		return new VehicleDocumentResponse(
				e.getId(),
				e.getVehicleId(),
				e.getDocType(),
				e.getExpiresOn(),
				e.getLastRenewedOn(),
				e.getRenewalPeriodMonths(),
				e.getLastAmount(),
				e.getNotes());
	}
}
