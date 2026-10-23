package com.djiguiya.transport.shared.util;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Djiguiya fiscal year: 1 August → 31 July.
 * Fiscal year {@code Y} covers {@code Y-08} … {@code (Y+1)-07}.
 */
public final class FiscalYearUtil {

	public static final int START_MONTH = 8;

	private FiscalYearUtil() {
	}

	/** Calendar year in which the fiscal year starts (e.g. 2025 for Aug 2025–Jul 2026). */
	public static int startYearContaining(LocalDate date) {
		if (date.getMonthValue() >= START_MONTH) {
			return date.getYear();
		}
		return date.getYear() - 1;
	}

	/** First ledger month key in the fiscal year (August of {@code fiscalStartYear}). */
	public static String startMonthKey(int fiscalStartYear) {
		return MonthKeyUtil.forMonthYear(START_MONTH, fiscalStartYear);
	}

	/** Last ledger month key in the fiscal year (July of {@code fiscalStartYear + 1}). */
	public static String endMonthKey(int fiscalStartYear) {
		return MonthKeyUtil.forMonthYear(7, fiscalStartYear + 1);
	}

	/** Inclusive start date of the fiscal year (1 August). */
	public static LocalDate startDate(int fiscalStartYear) {
		return LocalDate.of(fiscalStartYear, START_MONTH, 1);
	}

	/** Inclusive end date of the fiscal year (last day of July). */
	public static LocalDate endDate(int fiscalStartYear) {
		return YearMonth.of(fiscalStartYear + 1, 7).atEndOfMonth();
	}
}
