package com.djiguiya.transport.shared.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public final class MonthKeyUtil {

	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

	private MonthKeyUtil() {
	}

	public static String fromDate(LocalDate date) {
		return YearMonth.from(date).format(FORMAT);
	}

	public static String forMonthYear(int month, int year) {
		return YearMonth.of(year, month).format(FORMAT);
	}

	public static YearMonth parse(String monthKey) {
		return YearMonth.parse(monthKey, FORMAT);
	}
}
