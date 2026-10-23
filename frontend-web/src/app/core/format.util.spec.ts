import { describe, expect, it } from 'vitest';
import { formatXof } from './format.util';

/**
 * formatXof: West African CFA display for numbers; em dash for missing or invalid values.
 */
describe('formatXof', () => {
  /** null/undefined/NaN render as em dash; positive amounts include grouping and currency hint. */
  it('formats amounts and handles empty', () => {
    expect(formatXof(null)).toBe('—');
    expect(formatXof(undefined)).toBe('—');
    expect(formatXof(Number.NaN)).toBe('—');
    expect(formatXof(1500)).toContain('1');
    expect(formatXof(1500)).toMatch(/XOF|F/);
  });
});
