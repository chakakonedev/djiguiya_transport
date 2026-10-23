package com.djiguiya.transport.modules.trips.service;

import com.djiguiya.transport.modules.fleet.service.VehicleService;
import com.djiguiya.transport.modules.staff.service.StaffService;
import com.djiguiya.transport.modules.trips.dto.TripRequest;
import com.djiguiya.transport.modules.trips.dto.TripResponse;
import com.djiguiya.transport.modules.trips.entities.TripEntity;
import com.djiguiya.transport.modules.trips.repository.TripRepository;
import com.djiguiya.transport.shared.exception.BusinessException;
import com.djiguiya.transport.shared.util.EntityIds;
import com.djiguiya.transport.shared.util.MonthKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages trip lifecycle: validates fleet/staff references, derives accounting month key, and enriches API responses.
 */
@Service
@RequiredArgsConstructor
public class TripService {

	private final TripRepository tripRepository;
	private final VehicleService vehicleService;
	private final StaffService staffService;

	@Transactional(readOnly = true)
	public List<TripResponse> listAll() {
		return tripRepository.findByDeletedAtIsNullOrderByDepartureDateDesc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public TripResponse getById(String id) {
		return toResponse(findEntity(id));
	}

	@Transactional
	public TripResponse create(TripRequest request) {
		validateDates(request);
		vehicleService.findVehicle(request.vehicleId());
		String driverId = resolveDriverId(request);
		staffService.findEntity(driverId);

		TripEntity entity = new TripEntity();
		entity.setId(EntityIds.newId());
		apply(entity, request, driverId);
		return toResponse(tripRepository.save(entity));
	}

	@Transactional
	public TripResponse update(String id, TripRequest request) {
		validateDates(request);
		TripEntity entity = findEntity(id);
		vehicleService.findVehicle(request.vehicleId());
		String driverId = resolveDriverId(request);
		staffService.findEntity(driverId);
		apply(entity, request, driverId);
		return toResponse(tripRepository.save(entity));
	}

	/** Loads a non-deleted trip; used by finance and other modules. */
	public TripEntity findEntity(String id) {
		return tripRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new BusinessException("trip.not_found", HttpStatus.NOT_FOUND));
	}

	private TripResponse toResponse(TripEntity entity) {
		String vehicleName = vehicleService.findVehicle(entity.getVehicleId()).getName();
		String driverName = staffService.findEntity(entity.getDriverId()).getFullName();
		return TripResponse.from(entity, vehicleName, driverName);
	}

	private void validateDates(TripRequest request) {
		if (request.departureDate().isAfter(request.returnDate())) {
			throw new BusinessException("trip.departure_after_return", HttpStatus.BAD_REQUEST);
		}
	}

	/** Explicit driver wins; otherwise use primary assignment effective on departure date. */
	private String resolveDriverId(TripRequest request) {
		if (request.driverId() != null && !request.driverId().isBlank()) {
			return request.driverId();
		}
		return vehicleService.resolvePrimaryDriverId(request.vehicleId(), request.departureDate())
				.orElseThrow(() -> new BusinessException("trip.no_primary_driver", HttpStatus.BAD_REQUEST));
	}

	private void apply(TripEntity entity, TripRequest request, String driverId) {
		entity.setVehicleId(request.vehicleId());
		entity.setDepartureDate(request.departureDate());
		entity.setReturnDate(request.returnDate());
		entity.setMonthKey(MonthKeyUtil.fromDate(request.departureDate()));
		entity.setDriverId(driverId);
		entity.setReplacement(Boolean.TRUE.equals(request.replacement()));
		entity.setReplacementReason(request.replacementReason());
		entity.setNotes(request.notes());
	}
}
