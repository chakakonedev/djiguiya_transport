package com.djiguiya.transport.modules.trips.repository;

import com.djiguiya.transport.modules.trips.entities.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<TripEntity, String> {

	List<TripEntity> findByDeletedAtIsNullOrderByDepartureDateDesc();

	Optional<TripEntity> findByIdAndDeletedAtIsNull(String id);

	List<TripEntity> findByMonthKeyAndDeletedAtIsNull(String monthKey);
}
