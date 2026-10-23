package com.djiguiya.transport.modules.finance.controller;

import com.djiguiya.transport.modules.finance.dto.StatsCompareMonthResponse;
import com.djiguiya.transport.modules.finance.dto.StatsMonthlySeriesResponse;
import com.djiguiya.transport.modules.finance.dto.StatsSummaryResponse;
import com.djiguiya.transport.modules.finance.dto.StatsYoyResponse;
import com.djiguiya.transport.modules.finance.service.StatsService;
import com.djiguiya.transport.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for ledger analytics: filtered summaries, fiscal-year monthly series,
 * month-over-month comparison, and year-over-year net profit.
 */
@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class StatsController {

	private final StatsService statsService;

	/**
	 * Aggregates revenue, expense, net profit, trip count, and expense breakdown for optional
	 * filters (single calendar month, entire fiscal year, vehicle, driver, category).
	 */
	@GetMapping("/summary")
	public ApiResponse<StatsSummaryResponse> summary(
			@RequestParam(required = false) String vehicleId,
			@RequestParam(required = false) String driverId,
			@RequestParam(required = false) Integer month,
			@RequestParam(required = false) Integer year,
			@RequestParam(required = false) String category) {
		return ApiResponse.ok(statsService.summary(vehicleId, driverId, month, year, category));
	}

	/**
	 * Returns twelve monthly data points for the fiscal year starting in {@code year}
	 * (August through July), optionally scoped to a vehicle or driver.
	 */
	@GetMapping("/monthly")
	public ApiResponse<StatsMonthlySeriesResponse> monthly(
			@RequestParam int year,
			@RequestParam(required = false) String vehicleId,
			@RequestParam(required = false) String driverId) {
		return ApiResponse.ok(statsService.monthlySeries(year, vehicleId, driverId));
	}

	/**
	 * Compares net profit for the given calendar month index in the current year
	 * against the immediately preceding calendar month.
	 */
	@GetMapping("/compare-month")
	public ApiResponse<StatsCompareMonthResponse> compareMonth(
			@RequestParam int monthIndex,
			@RequestParam(required = false) String vehicleId,
			@RequestParam(required = false) String driverId) {
		return ApiResponse.ok(statsService.compareMonth(monthIndex, vehicleId, driverId));
	}

	/**
	 * Compares total net profit for fiscal year {@code year} against the prior fiscal year,
	 * including absolute delta and percentage change when the baseline is non-zero.
	 */
	@GetMapping("/yoy")
	public ApiResponse<StatsYoyResponse> yoy(
			@RequestParam int year,
			@RequestParam(required = false) String vehicleId,
			@RequestParam(required = false) String driverId) {
		return ApiResponse.ok(statsService.yearOverYear(year, vehicleId, driverId));
	}
}
