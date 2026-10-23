package com.djiguiya.transport.shared.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MonthKeyUtilTest {

	@Test
	void fromDate_andForMonthYear_andParse() {
		assertEquals("2026-03", MonthKeyUtil.fromDate(LocalDate.of(2026, 3, 15)));
		assertEquals("2025-08", MonthKeyUtil.forMonthYear(8, 2025));
		assertEquals(YearMonth.of(2026, 7), MonthKeyUtil.parse("2026-07"));
	}
}
