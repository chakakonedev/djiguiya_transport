package com.djiguiya.transport.modules.fleet.dto;

import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;

import java.time.LocalDate;

public record VehicleResponse(
		String id,
		String name,
		String plate,
		String brand,
		String model,
		Integer yearBuilt,
		Long purchasePrice,
		LocalDate purchaseDate,
		String status,
		String notes,
		String primaryDriverId
) {
	public static VehicleResponse from(VehicleEntity e, String primaryDriverId) {
		return new VehicleResponse(
				e.getId(),
				e.getName(),
				e.getPlate(),
				e.getBrand(),
				e.getModel(),
				e.getYearBuilt(),
				e.getPurchasePrice(),
				e.getPurchaseDate(),
				e.getStatus(),
				e.getNotes(),
				primaryDriverId);
	}
}
