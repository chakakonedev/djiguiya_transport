package com.djiguiya.transport.modules.finance.entities;

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
@Table(name = "transactions")
@EntityListeners(AuditingListener.class)
@Getter
@Setter
public class TransactionEntity implements Timestamped {

	@Id
	@Column(length = 36)
	private String id;

	@Column(nullable = false, length = 20)
	private String type;

	@Column(name = "txn_date", nullable = false)
	private LocalDate txnDate;

	@Column(name = "month_key", nullable = false, length = 7)
	private String monthKey;

	@Column(nullable = false)
	private Long amount;

	@Column(name = "vehicle_id", length = 36)
	private String vehicleId;

	@Column(name = "driver_id", length = 36)
	private String driverId;

	@Column(name = "trip_id", length = 36)
	private String tripId;

	@Column(name = "category_code", length = 40)
	private String categoryCode;

	@Column(name = "subcategory_code", length = 40)
	private String subcategoryCode;

	@Column(nullable = false, length = 255)
	private String label;

	@Column(name = "payroll_month", length = 7)
	private String payrollMonth;

	@Column(name = "payroll_kind", length = 20)
	private String payrollKind;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_by", length = 36)
	private String createdBy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "deleted_at")
	private Instant deletedAt;
}
