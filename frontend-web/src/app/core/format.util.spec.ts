import { describe, expect, it } from 'vitest';
import { formatXof } from './format.util';

describe('formatXof', () => {
  it('formats amounts and handles empty', () => {
    expect(formatXof(null)).toBe('—');
    expect(formatXof(undefined)).toBe('—');
    expect(formatXof(Number.NaN)).toBe('—');
    expect(formatXof(1500)).toContain('1');
    expect(formatXof(1500)).toMatch(/XOF|F/);
  });
});
