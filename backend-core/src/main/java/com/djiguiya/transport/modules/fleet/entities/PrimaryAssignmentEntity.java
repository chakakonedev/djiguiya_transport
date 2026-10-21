package com.djiguiya.transport.modules.fleet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "primary_assignments")
@Getter
@Setter
public class PrimaryAssignmentEntity {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "vehicle_id", nullable = false, length = 36)
	private String vehicleId;

	@Column(name = "driver_id", nullable = false, length = 36)
	private String driverId;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(length = 255)
	private String reason;
}
