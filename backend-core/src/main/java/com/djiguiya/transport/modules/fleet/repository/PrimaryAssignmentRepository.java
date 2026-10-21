package com.djiguiya.transport.modules.fleet.repository;

import com.djiguiya.transport.modules.fleet.entities.PrimaryAssignmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface PrimaryAssignmentRepository extends JpaRepository<PrimaryAssignmentEntity, String> {

	@Query("""
			select a from PrimaryAssignmentEntity a
			where a.vehicleId = :vehicleId
			and a.startDate <= :onDate
			and (a.endDate is null or a.endDate >= :onDate)
			order by a.startDate desc
			""")
	Optional<PrimaryAssignmentEntity> findActiveForVehicleOnDate(
			@Param("vehicleId") String vehicleId,
			@Param("onDate") LocalDate onDate);
}
