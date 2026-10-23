package com.djiguiya.transport.modules.trips.controller;

import com.djiguiya.transport.modules.trips.dto.TripRequest;
import com.djiguiya.transport.modules.trips.dto.TripResponse;
import com.djiguiya.transport.modules.trips.service.TripService;
import com.djiguiya.transport.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST API for vehicle trips: scheduling, driver assignment, and soft-deleted-aware listing.
 */
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {

	private final TripService tripService;

	/** Lists non-deleted trips, newest departure first. */
	@GetMapping
	public ApiResponse<List<TripResponse>> list() {
		return ApiResponse.ok(tripService.listAll());
	}

	/** Returns one trip with resolved vehicle and driver display names. */
	@GetMapping("/{id}")
	public ApiResponse<TripResponse> get(@PathVariable String id) {
		return ApiResponse.ok(tripService.getById(id));
	}

	/** Creates a trip; driver may be omitted to use the vehicle's primary driver on the departure date. */
	@PostMapping
	public ApiResponse<TripResponse> create(@Valid @RequestBody TripRequest request) {
		return ApiResponse.ok("trip.created", tripService.create(request));
	}

	/** Updates trip dates, vehicle, driver, and replacement metadata. */
	@PutMapping("/{id}")
	public ApiResponse<TripResponse> update(@PathVariable String id, @Valid @RequestBody TripRequest request) {
		return ApiResponse.ok("trip.updated", tripService.update(id, request));
	}
}
