package com.djiguiya.transport.modules.fleet.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "vehicle_documents")
@Getter
@Setter
public class VehicleDocumentEntity {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "vehicle_id", nullable = false, length = 36)
	private String vehicleId;

	@Column(name = "doc_type", nullable = false, length = 40)
	private String docType;

	@Column(name = "expires_on", nullable = false)
	private LocalDate expiresOn;

	@Column(name = "last_renewed_on")
	private LocalDate lastRenewedOn;

	@Column(name = "renewal_period_months", nullable = false)
	private int renewalPeriodMonths = 12;

	@Column(name = "last_amount")
	private Long lastAmount;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	@PreUpdate
	void touchUpdatedAt() {
		updatedAt = Instant.now();
	}
}
