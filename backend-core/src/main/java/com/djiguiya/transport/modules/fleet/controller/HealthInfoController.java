package com.djiguiya.transport.modules.fleet.controller;

import com.djiguiya.transport.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class HealthInfoController {

	@GetMapping("/ping")
	public ApiResponse<Map<String, String>> ping() {
		return ApiResponse.ok(Map.of(
				"app", "djiguiya-transport",
				"status", "ok"
		));
	}
}
