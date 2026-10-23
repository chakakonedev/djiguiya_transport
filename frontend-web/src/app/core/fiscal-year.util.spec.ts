import { describe, expect, it } from 'vitest';
import {
  calendarYearForFiscalMonth,
  fiscalPeriodLabel,
  fiscalStartYearContaining,
  monthI18nKey,
  monthShortI18nKey
} from './fiscal-year.util';

describe('fiscal-year.util', () => {
  it('fiscalStartYearContaining uses August boundary', () => {
    expect(fiscalStartYearContaining(new Date(2026, 7, 1))).toBe(2026); // Aug
    expect(fiscalStartYearContaining(new Date(2026, 6, 31))).toBe(2025); // Jul
  });

  it('fiscalPeriodLabel and calendarYearForFiscalMonth', () => {
    expect(fiscalPeriodLabel(2025)).toEqual({ start: '2025-08', end: '2026-07' });
    expect(calendarYearForFiscalMonth(2025, 8)).toBe(2025);
    expect(calendarYearForFiscalMonth(2025, 1)).toBe(2026);
  });

  it('month i18n keys', () => {
    expect(monthI18nKey(3)).toBe('common.months.3');
    expect(monthShortI18nKey(12)).toBe('common.monthsShort.12');
  });
});
