package com.djiguiya.transport.modules.staff.controller;

import com.djiguiya.transport.modules.staff.dto.StaffRequest;
import com.djiguiya.transport.modules.staff.dto.StaffResponse;
import com.djiguiya.transport.modules.staff.service.StaffService;
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
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

	private final StaffService staffService;

	@GetMapping
	public ApiResponse<List<StaffResponse>> list() {
		return ApiResponse.ok(staffService.listAll());
	}

	@GetMapping("/{id}")
	public ApiResponse<StaffResponse> get(@PathVariable String id) {
		return ApiResponse.ok(staffService.getById(id));
	}

	@PostMapping
	public ApiResponse<StaffResponse> create(@Valid @RequestBody StaffRequest request) {
		return ApiResponse.ok("staff.created", staffService.create(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<StaffResponse> update(@PathVariable String id, @Valid @RequestBody StaffRequest request) {
		return ApiResponse.ok("staff.updated", staffService.update(id, request));
	}
}
