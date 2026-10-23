package com.djiguiya.transport.modules.staff.entities;

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

/**
 * Person employed or contracted by the transport operation (driver, manager, etc.).
 * Referenced by trips ({@code driverId}) and primary vehicle assignments; optional {@code userId} ties to login.
 */
@Entity
@Table(name = "staff")
@EntityListeners(AuditingListener.class)
@Getter
@Setter
public class StaffEntity implements Timestamped {

	@Id
	@Column(length = 36)
	private String id;

	@Column(name = "full_name", nullable = false, length = 180)
	private String fullName;

	@Column(name = "staff_type", nullable = false, length = 40)
	private String staffType;

	@Column(length = 40)
	private String phone;

	@Column(nullable = false, length = 20)
	private String status = "active";

	@Column(name = "monthly_salary")
	private Long monthlySalary;

	@Column(name = "pay_day")
	private Integer payDay;

	@Column(name = "user_id", length = 36)
	private String userId;

	@Column(columnDefinition = "TEXT")
	private String notes;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
