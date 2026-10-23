package com.djiguiya.transport.shared.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Canonical {@code yyyy-MM} keys for grouping ledger entries and month-scoped reporting.
 */
public final class MonthKeyUtil {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

	private MonthKeyUtil() {
	}

	/** Month bucket for a calendar date (trip or transaction day). */
	public static String fromDate(LocalDate date) {
		return YearMonth.from(date).format(FORMAT);
	}

	/** Builds a key for fiscal/report boundaries (see {@link FiscalYearUtil}). */
	public static String forMonthYear(int month, int year) {
		return YearMonth.of(year, month).format(FORMAT);
	}

	/** Parses API/query month keys back to {@link YearMonth}. */
	public static YearMonth parse(String monthKey) {
		return YearMonth.parse(monthKey, FORMAT);
	}
}
