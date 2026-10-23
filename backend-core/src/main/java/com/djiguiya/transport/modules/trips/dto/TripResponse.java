package com.djiguiya.transport.modules.trips.dto;

import com.djiguiya.transport.modules.trips.entities.TripEntity;

import java.time.LocalDate;

/**
 * Trip view for API clients, including denormalized vehicle and driver names for display.
 */
public record TripResponse(
		String id,
		String vehicleId,
		String vehicleName,
		LocalDate departureDate,
		LocalDate returnDate,
		String monthKey,
		String driverId,
		String driverName,
		boolean replacement,
		String replacementReason,
		String notes
) {
	public static TripResponse from(TripEntity e, String vehicleName, String driverName) {
		return new TripResponse(
				e.getId(),
				e.getVehicleId(),
				vehicleName,
				e.getDepartureDate(),
				e.getReturnDate(),
				e.getMonthKey(),
				e.getDriverId(),
				driverName,
				e.isReplacement(),
				e.getReplacementReason(),
				e.getNotes());
	}
}
