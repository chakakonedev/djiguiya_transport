package com.djiguiya.transport.modules.fleet.controller;

import com.djiguiya.transport.modules.fleet.dto.PrimaryDriverRequest;
import com.djiguiya.transport.modules.fleet.dto.VehicleDocumentRequest;
import com.djiguiya.transport.modules.fleet.dto.VehicleDocumentResponse;
import com.djiguiya.transport.modules.fleet.dto.VehicleRequest;
import com.djiguiya.transport.modules.fleet.dto.VehicleResponse;
import com.djiguiya.transport.modules.fleet.service.VehicleService;
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

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {

	private final VehicleService vehicleService;

	@GetMapping
	public ApiResponse<List<VehicleResponse>> list() {
		return ApiResponse.ok(vehicleService.listAll());
	}

	@GetMapping("/{id}")
	public ApiResponse<VehicleResponse> get(@PathVariable String id) {
		return ApiResponse.ok(vehicleService.getById(id));
	}

	@PostMapping
	public ApiResponse<VehicleResponse> create(@Valid @RequestBody VehicleRequest request) {
		return ApiResponse.ok("vehicle.created", vehicleService.create(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<VehicleResponse> update(@PathVariable String id, @Valid @RequestBody VehicleRequest request) {
		return ApiResponse.ok("vehicle.updated", vehicleService.update(id, request));
	}

	@PostMapping("/{id}/primary-driver")
	public ApiResponse<VehicleResponse> assignPrimaryDriver(
			@PathVariable String id,
			@Valid @RequestBody PrimaryDriverRequest request) {
		return ApiResponse.ok("vehicle.primary_driver_set", vehicleService.assignPrimaryDriver(id, request));
	}

	@GetMapping("/{id}/documents")
	public ApiResponse<List<VehicleDocumentResponse>> listDocuments(@PathVariable String id) {
		return ApiResponse.ok(vehicleService.listDocuments(id));
	}

	@PutMapping("/{id}/documents")
	public ApiResponse<VehicleDocumentResponse> upsertDocument(
			@PathVariable String id,
			@Valid @RequestBody VehicleDocumentRequest request) {
		return ApiResponse.ok("document.updated", vehicleService.upsertDocument(id, request));
	}
}
