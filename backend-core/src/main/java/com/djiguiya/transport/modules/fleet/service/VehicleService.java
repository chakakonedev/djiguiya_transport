package com.djiguiya.transport.modules.fleet.service;

import com.djiguiya.transport.modules.fleet.dto.PrimaryDriverRequest;
import com.djiguiya.transport.modules.fleet.dto.VehicleDocumentRequest;
import com.djiguiya.transport.modules.fleet.dto.VehicleDocumentResponse;
import com.djiguiya.transport.modules.fleet.dto.VehicleRequest;
import com.djiguiya.transport.modules.fleet.dto.VehicleResponse;
import com.djiguiya.transport.modules.fleet.entities.PrimaryAssignmentEntity;
import com.djiguiya.transport.modules.fleet.entities.VehicleDocumentEntity;
import com.djiguiya.transport.modules.fleet.entities.VehicleEntity;
import com.djiguiya.transport.modules.fleet.repository.PrimaryAssignmentRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleDocumentRepository;
import com.djiguiya.transport.modules.fleet.repository.VehicleRepository;
import com.djiguiya.transport.modules.staff.service.StaffService;
import com.djiguiya.transport.shared.exception.BusinessException;
import com.djiguiya.transport.shared.util.EntityIds;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Fleet registry: vehicles, time-bounded primary driver assignments, and per-vehicle compliance documents.
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

	private final VehicleRepository vehicleRepository;
	private final PrimaryAssignmentRepository primaryAssignmentRepository;
	private final VehicleDocumentRepository vehicleDocumentRepository;
	private final StaffService staffService;

	@Transactional(readOnly = true)
	public List<VehicleResponse> listAll() {
		return vehicleRepository.findAll().stream()
				.map(v -> VehicleResponse.from(v, resolvePrimaryDriverId(v.getId(), LocalDate.now()).orElse(null)))
				.toList();
	}

	@Transactional(readOnly = true)
	public VehicleResponse getById(String id) {
		VehicleEntity vehicle = findVehicle(id);
		String driverId = resolvePrimaryDriverId(id, LocalDate.now()).orElse(null);
		return VehicleResponse.from(vehicle, driverId);
	}

	@Transactional
	public VehicleResponse create(VehicleRequest request) {
		VehicleEntity entity = new VehicleEntity();
		entity.setId(EntityIds.newId());
		apply(entity, request);
		return VehicleResponse.from(vehicleRepository.save(entity), null);
	}

	@Transactional
	public VehicleResponse update(String id, VehicleRequest request) {
		VehicleEntity entity = findVehicle(id);
		apply(entity, request);
		String driverId = resolvePrimaryDriverId(id, LocalDate.now()).orElse(null);
		return VehicleResponse.from(vehicleRepository.save(entity), driverId);
	}

	/**
	 * Starts a new primary-driver assignment and ends overlapping open rows so history stays non-overlapping.
	 */
	@Transactional
	public VehicleResponse assignPrimaryDriver(String vehicleId, PrimaryDriverRequest request) {
		VehicleEntity vehicle = findVehicle(vehicleId);
		staffService.findEntity(request.driverId());

		LocalDate startDate = request.startDate();
		closeOpenAssignments(vehicleId, startDate);

		PrimaryAssignmentEntity assignment = new PrimaryAssignmentEntity();
		assignment.setId(EntityIds.newId());
		assignment.setVehicleId(vehicleId);
		assignment.setDriverId(request.driverId());
		assignment.setStartDate(startDate);
		assignment.setEndDate(request.endDate());
		assignment.setReason(request.reason());
		primaryAssignmentRepository.save(assignment);
		primaryAssignmentRepository.flush();

		String driverId = resolvePrimaryDriverId(vehicleId, LocalDate.now()).orElse(null);
		return VehicleResponse.from(vehicle, driverId);
	}

	private void closeOpenAssignments(String vehicleId, LocalDate newStartDate) {
		List<PrimaryAssignmentEntity> open =
				primaryAssignmentRepository.findByVehicleIdAndEndDateIsNullOrderByStartDateAsc(vehicleId);
		for (PrimaryAssignmentEntity active : open) {
			LocalDate previousEnd = newStartDate.minusDays(1);
			// Same-day (or earlier) reassignment: collapse the previous row to a one-day span.
			active.setEndDate(previousEnd.isBefore(active.getStartDate())
					? active.getStartDate()
					: previousEnd);
			primaryAssignmentRepository.save(active);
		}
		primaryAssignmentRepository.flush();
	}

	@Transactional(readOnly = true)
	public List<VehicleDocumentResponse> listDocuments(String vehicleId) {
		findVehicle(vehicleId);
		return vehicleDocumentRepository.findByVehicleIdOrderByDocTypeAsc(vehicleId).stream()
				.map(VehicleDocumentResponse::from)
				.toList();
	}

	@Transactional
	public List<VehicleDocumentResponse> replaceDocuments(String vehicleId, List<VehicleDocumentRequest> requests) {
		findVehicle(vehicleId);
		return requests.stream().map(request -> upsertDocumentEntity(vehicleId, request)).toList();
	}

	@Transactional
	public VehicleDocumentResponse upsertDocument(String vehicleId, VehicleDocumentRequest request) {
		findVehicle(vehicleId);
		return upsertDocumentEntity(vehicleId, request);
	}

	private VehicleDocumentResponse upsertDocumentEntity(String vehicleId, VehicleDocumentRequest request) {
		VehicleDocumentEntity doc = vehicleDocumentRepository
				.findByVehicleIdAndDocType(vehicleId, request.docType())
				.orElseGet(() -> {
					VehicleDocumentEntity created = new VehicleDocumentEntity();
					created.setId(EntityIds.newId());
					created.setVehicleId(vehicleId);
					return created;
				});
		doc.setDocType(request.docType());
		doc.setExpiresOn(request.expiresOn());
		doc.setLastRenewedOn(request.lastRenewedOn());
		doc.setRenewalPeriodMonths(
				request.renewalPeriodMonths() != null ? request.renewalPeriodMonths() : 12);
		doc.setLastAmount(request.lastAmount());
		doc.setNotes(request.notes());
		return VehicleDocumentResponse.from(vehicleDocumentRepository.save(doc));
	}

	/** Primary driver effective on {@code onDate}, if any assignment covers that day. */
	public Optional<String> resolvePrimaryDriverId(String vehicleId, LocalDate onDate) {
		return primaryAssignmentRepository.findActiveForVehicleOnDate(vehicleId, onDate)
				.map(PrimaryAssignmentEntity::getDriverId);
	}

	public VehicleEntity findVehicle(String id) {
		return vehicleRepository.findById(id)
				.orElseThrow(() -> new BusinessException("vehicle.not_found", HttpStatus.NOT_FOUND));
	}

	private void apply(VehicleEntity entity, VehicleRequest request) {
		entity.setName(request.name().trim());
		entity.setPlate(request.plate());
		entity.setBrand(request.brand());
		entity.setModel(request.model());
		entity.setYearBuilt(request.yearBuilt());
		entity.setPurchasePrice(request.purchasePrice());
		entity.setPurchaseDate(request.purchaseDate());
		entity.setStatus(request.status() != null ? request.status() : "active");
		entity.setNotes(request.notes());
	}
}
