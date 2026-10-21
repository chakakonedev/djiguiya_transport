package com.djiguiya.transport.modules.staff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StaffRequest(
		@NotBlank @Size(max = 180) String fullName,
		@NotBlank @Size(max = 40) String staffType,
		@Size(max = 40) String phone,
		@Size(max = 20) String status,
		Long monthlySalary,
		Integer payDay,
		@Size(max = 36) String userId,
		String notes
) {
}
