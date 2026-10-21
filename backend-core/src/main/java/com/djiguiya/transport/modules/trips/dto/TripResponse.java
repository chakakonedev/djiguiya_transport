package com.djiguiya.transport.modules.trips.dto;

import com.djiguiya.transport.modules.trips.entities.TripEntity;

import java.time.LocalDate;

public record TripResponse(
		String id,
		String vehicleId,
		LocalDate departureDate,
		LocalDate returnDate,
		String monthKey,
		String driverId,
		boolean replacement,
		String replacementReason,
		String notes
) {
	public static TripResponse from(TripEntity e) {
		return new TripResponse(
				e.getId(),
				e.getVehicleId(),
				e.getDepartureDate(),
				e.getReturnDate(),
				e.getMonthKey(),
				e.getDriverId(),
				e.isReplacement(),
				e.getReplacementReason(),
				e.getNotes());
	}
}
