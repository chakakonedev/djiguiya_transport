package com.djiguiya.transport.modules.fleet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VehicleRequest(
		@NotBlank @Size(max = 120) String name,
		@Size(max = 60) String plate,
		@Size(max = 80) String brand,
		@Size(max = 80) String model,
		Integer yearBuilt,
		Long purchasePrice,
		LocalDate purchaseDate,
		@Size(max = 20) String status,
		String notes
) {
}
