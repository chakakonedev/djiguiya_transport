package com.djiguiya.transport.modules.staff.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Historical link between a manager staff id and a driver staff id, with optional effective date range.
 */
@Entity
@Table(name = "team_links")
@Getter
@Setter
public class TeamLinkEntity {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "manager_id", nullable = false, length = 36)
	private String managerId;

	@Column(name = "driver_id", nullable = false, length = 36)
	private String driverId;

	@Column(name = "start_date")
	private LocalDate startDate;

	/** When set, the assignment no longer applies after this date. */
	@Column(name = "end_date")
	private LocalDate endDate;
}
