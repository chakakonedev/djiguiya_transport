package com.djiguiya.transport.modules.finance.controller;

import com.djiguiya.transport.modules.finance.dto.ProfitSharePreviewResponse;
import com.djiguiya.transport.modules.finance.dto.ProfitShareRuleResponse;
import com.djiguiya.transport.modules.finance.dto.ProfitShareRuleUpdateRequest;
import com.djiguiya.transport.modules.finance.service.ProfitShareRuleService;
import com.djiguiya.transport.modules.finance.service.ProfitShareService;
import com.djiguiya.transport.shared.api.ApiResponse;
import com.djiguiya.transport.shared.security.AppRoles;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints for annual profit-share preview and configuration of distribution rules
 * (reserve percentage, partner shares, quarterly salary bonuses).
 */
@RestController
@RequestMapping("/api/v1/profit-share")
@RequiredArgsConstructor
public class ProfitShareController {

	private final ProfitShareService profitShareService;
	private final ProfitShareRuleService profitShareRuleService;

	/**
	 * Simulates fiscal-year profit allocation: net profit, company reserve, distributable pool,
	 * and per-partner shares from active rules at fiscal year end.
	 */
	@GetMapping("/preview")
	public ApiResponse<ProfitSharePreviewResponse> preview(@RequestParam int year) {
		return ApiResponse.ok(profitShareService.preview(year));
	}

	/** Lists all profit-share rules with resolved beneficiary display names. */
	@GetMapping("/rules")
	public ApiResponse<List<ProfitShareRuleResponse>> listRules() {
		return ApiResponse.ok(profitShareRuleService.listAll());
	}

	/** Partially updates rate, salary-month equivalent, enabled flag, or notes (owner only). */
	@PutMapping("/rules/{id}")
	@PreAuthorize(AppRoles.OWNER_ONLY)
	public ApiResponse<ProfitShareRuleResponse> updateRule(
			@PathVariable String id,
			@Valid @RequestBody ProfitShareRuleUpdateRequest request) {
		return ApiResponse.ok("profit_share.rule_updated", profitShareRuleService.update(id, request));
	}
}
