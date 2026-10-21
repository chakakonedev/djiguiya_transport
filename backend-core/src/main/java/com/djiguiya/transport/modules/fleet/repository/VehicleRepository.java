package com.djiguiya.transport.modules.fleet.repository;

import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<VehicleEntity, String> {

	List<VehicleEntity> findByStatusOrderByNameAsc(String status);
}
