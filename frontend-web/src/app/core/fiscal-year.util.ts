/** Fiscal year starts in August (1 Aug → 31 Jul). */
export const FISCAL_START_MONTH = 8;

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
