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

@Service
@RequiredArgsConstructor
public class TripService {

	private final TripRepository tripRepository;
	private final VehicleService vehicleService;
	private final StaffService staffService;

	@Transactional(readOnly = true)
	public List<TripResponse> listAll() {
		return tripRepository.findByDeletedAtIsNullOrderByDepartureDateDesc().stream()
				.map(TripResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public TripResponse getById(String id) {
		return TripResponse.from(findEntity(id));
	}

	@Transactional
	public TripResponse create(TripRequest request) {
		vehicleService.findVehicle(request.vehicleId());
		String driverId = resolveDriverId(request);
		staffService.findEntity(driverId);

		TripEntity entity = new TripEntity();
		entity.setId(EntityIds.newId());
		apply(entity, request, driverId);
		return TripResponse.from(tripRepository.save(entity));
	}

	@Transactional
	public TripResponse update(String id, TripRequest request) {
		TripEntity entity = findEntity(id);
		vehicleService.findVehicle(request.vehicleId());
		String driverId = resolveDriverId(request);
		staffService.findEntity(driverId);
		apply(entity, request, driverId);
		return TripResponse.from(tripRepository.save(entity));
	}

	public TripEntity findEntity(String id) {
		return tripRepository.findByIdAndDeletedAtIsNull(id)
				.orElseThrow(() -> new BusinessException("trip.not_found", HttpStatus.NOT_FOUND));
	}

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
