package com.djiguiya.transport.modules.finance.controller;

import com.djiguiya.transport.modules.finance.dto.StatsCompareMonthResponse;
import com.djiguiya.transport.modules.finance.dto.StatsSummaryResponse;
import com.djiguiya.transport.modules.finance.dto.StatsYoyResponse;
import com.djiguiya.transport.modules.finance.service.StatsService;
import com.djiguiya.transport.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

	private final StatsService statsService;

	@GetMapping("/summary")
	public ApiResponse<StatsSummaryResponse> summary(
			@RequestParam(required = false) String vehicleId,
			@RequestParam(required = false) String driverId,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) String category) {
		return ApiResponse.ok(statsService.summary(vehicleId, driverId, month, year, category));
	}

	@GetMapping("/compare-month")
	public ApiResponse<StatsCompareMonthResponse> compareMonth(
			@RequestParam int monthIndex,
			@RequestParam(required = false) String vehicleId,
			@RequestParam(required = false) String driverId) {
		return ApiResponse.ok(statsService.compareMonth(monthIndex, vehicleId, driverId));
	}

	@GetMapping("/yoy")
	public ApiResponse<StatsYoyResponse> yoy(
			@RequestParam int year,
			@RequestParam(required = false) String vehicleId,
			@RequestParam(required = false) String driverId) {
		return ApiResponse.ok(statsService.yearOverYear(year, vehicleId, driverId));
	}
}
