/**
 * Fiscal calendar helpers (Aug–Jul). Stats and profit-share use fiscal start year
 * rather than calendar year for filtering and labels.
 */

/** Fiscal year starts in August (1 Aug → 31 Jul). */
export const FISCAL_START_MONTH = 8;

/** Calendar months in fiscal order: Aug … Jul. */
export const FISCAL_MONTHS = [8, 9, 10, 11, 12, 1, 2, 3, 4, 5, 6, 7] as const;

/** Calendar year in which the fiscal year starts (e.g. 2025 for Aug 2025–Jul 2026). */
export function fiscalStartYearContaining(date = new Date()): number {
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  return month >= FISCAL_START_MONTH ? year : year - 1;
}

export function fiscalPeriodLabel(fiscalStartYear: number): { start: string; end: string } {
  return {
    start: `${fiscalStartYear}-08`,
    end: `${fiscalStartYear + 1}-07`
  };
}

/** Calendar year for a month inside a fiscal year (Jan–Jul → fiscalStart + 1). */
export function calendarYearForFiscalMonth(
  fiscalStartYear: number,
  calendarMonth: number
): number {
  return calendarMonth >= FISCAL_START_MONTH ? fiscalStartYear : fiscalStartYear + 1;
}

export function monthI18nKey(calendarMonth: number): string {
  return `common.months.${calendarMonth}`;
}

export function monthShortI18nKey(calendarMonth: number): string {
  return `common.monthsShort.${calendarMonth}`;
}
