package com.djiguiya.transport.modules.finance.controller;

import com.djiguiya.transport.modules.finance.dto.ProfitSharePreviewResponse;
import com.djiguiya.transport.modules.finance.service.ProfitShareService;
import com.djiguiya.transport.shared.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profit-share")
@RequiredArgsConstructor
public class ProfitShareController {

	private final ProfitShareService profitShareService;

	@GetMapping("/preview")
	public ApiResponse<ProfitSharePreviewResponse> preview(@RequestParam int year) {
		return ApiResponse.ok(profitShareService.preview(year));
	}
}
