package com.djiguiya.transport.modules.fleet.entities;

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
@Table(name = "vehicles")
@EntityListeners(AuditingListener.class)
@Getter
@Setter
public class VehicleEntity implements Timestamped {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false, length = 120)
	private String name;

	@Column(length = 60)
	private String plate;

	@Column(length = 80)
	private String brand;

	@Column(length = 80)
	private String model;

	@Column(name = "year_built")
	private Integer yearBuilt;

	@Column(name = "purchase_price")
	private Long purchasePrice;

	@Column(name = "purchase_date")
	private LocalDate purchaseDate;

	@Column(nullable = false, length = 20)
	private String status = "active";

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
