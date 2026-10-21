package com.djiguiya.transport.modules.trips.entities;

import com.djiguiya.transport.shared.persistence.AuditingListener;
import com.djiguiya.transport.shared.persistence.Timestamped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "trips")
@EntityListeners(AuditingListener.class)
@Getter
@Setter
public class TripEntity implements Timestamped {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "vehicle_id", nullable = false, length = 36)
	private String vehicleId;

	@Column(name = "departure_date", nullable = false)
	private LocalDate departureDate;

	@Column(name = "return_date", nullable = false)
	private LocalDate returnDate;

	@Column(name = "month_key", nullable = false, length = 7)
	private String monthKey;

	@Column(name = "driver_id", nullable = false, length = 36)
	private String driverId;

	@Column(name = "is_replacement", nullable = false)
	private boolean replacement;

	@Column(name = "replacement_reason", length = 255)
	private String replacementReason;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;
}
