package com.djiguiya.transport.modules.staff.dto;

import com.djiguiya.transport.modules.staff.entities.StaffEntity;

/**
 * Staff profile exposed to clients. {@code userId} links to a login account when present.
 */
public record StaffResponse(
		String id,
		String fullName,
		String staffType,
		String phone,
		String status,
		Long monthlySalary,
		Integer payDay,
		String userId,
		String notes
) {
	public static StaffResponse from(StaffEntity e) {
		return new StaffResponse(
				e.getId(),
				e.getFullName(),
				e.getStaffType(),
				e.getPhone(),
				e.getStatus(),
				e.getMonthlySalary(),
				e.getPayDay(),
				e.getUserId(),
				e.getNotes());
	}
}
