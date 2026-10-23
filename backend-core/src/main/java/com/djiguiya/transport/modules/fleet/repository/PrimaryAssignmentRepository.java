package com.djiguiya.transport.modules.fleet.repository;

import com.djiguiya.transport.modules.fleet.entities.PrimaryAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrimaryAssignmentRepository extends JpaRepository<PrimaryAssignmentEntity, String> {

	List<PrimaryAssignmentEntity> findByVehicleIdAndEndDateIsNullOrderByStartDateAsc(String vehicleId);

	@Query("""
			select a from PrimaryAssignmentEntity a
			where a.vehicleId = :vehicleId
			and a.startDate <= :onDate
			and (a.endDate is null or a.endDate >= :onDate)
			order by a.startDate desc
			""")
	List<PrimaryAssignmentEntity> findActiveAssignmentsForVehicleOnDate(
			@Param("vehicleId") String vehicleId,
			@Param("onDate") LocalDate onDate);

	default Optional<PrimaryAssignmentEntity> findActiveForVehicleOnDate(String vehicleId, LocalDate onDate) {
		return findActiveAssignmentsForVehicleOnDate(vehicleId, onDate).stream().findFirst();
	}
}
