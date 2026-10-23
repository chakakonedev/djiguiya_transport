package com.djiguiya.transport.shared.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link FiscalYearUtil}: August–July fiscal year boundaries (no Spring).
 */
class FiscalYearUtilTest {

	/** Verifies fiscal start year rolls over on 1 August. */
	@Test
	void startYearContaining_switchesOnAugust() {
		assertEquals(2024, FiscalYearUtil.startYearContaining(LocalDate.of(2025, 7, 31)));
		assertEquals(2025, FiscalYearUtil.startYearContaining(LocalDate.of(2025, 8, 1)));
		assertEquals(2025, FiscalYearUtil.startYearContaining(LocalDate.of(2026, 7, 31)));
		assertEquals(2026, FiscalYearUtil.startYearContaining(LocalDate.of(2026, 8, 1)));
	}

	/** Verifies month keys and inclusive date range for fiscal year 2025. */
	@Test
	void year1_coversAugust2025ToJuly2026() {
		assertEquals("2025-08", FiscalYearUtil.startMonthKey(2025));
		assertEquals("2026-07", FiscalYearUtil.endMonthKey(2025));
		assertEquals(LocalDate.of(2025, 8, 1), FiscalYearUtil.startDate(2025));
		assertEquals(LocalDate.of(2026, 7, 31), FiscalYearUtil.endDate(2025));
	}
}
