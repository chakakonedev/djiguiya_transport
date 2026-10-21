package com.djiguiya.transport.modules.fleet.repository;

import com.djiguiya.transport.modules.fleet.entities.VehicleDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocumentEntity, String> {

	List<VehicleDocumentEntity> findByVehicleIdOrderByDocTypeAsc(String vehicleId);

	Optional<VehicleDocumentEntity> findByVehicleIdAndDocType(String vehicleId, String docType);
}
