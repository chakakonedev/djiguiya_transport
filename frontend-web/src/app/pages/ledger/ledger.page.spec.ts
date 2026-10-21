import { FormBuilder } from '@angular/forms';
import { describe, expect, it } from 'vitest';
import { revenueTripDatesValidator } from './ledger.page';

describe('revenueTripDatesValidator', () => {
  const fb = new FormBuilder();

  function form(values: {
    departureDate: string;
    returnDate: string;
    txnDate: string;
  }) {
    return fb.group(
      {
        departureDate: [values.departureDate],
        returnDate: [values.returnDate],
        txnDate: [values.txnDate]
      },
      { validators: revenueTripDatesValidator() }
    );
  }

  it('accepts departure ≤ return and payment on or after return', () => {
    const group = form({
      departureDate: '2026-10-23',
      returnDate: '2026-10-24',
      txnDate: '2026-10-25'
    });
    expect(group.valid).toBe(true);
  });

  it('rejects departure after return', () => {
    const group = form({
      departureDate: '2026-10-25',
      returnDate: '2026-10-24',
      txnDate: '2026-10-25'
    });
    expect(group.hasError('departureAfterReturn')).toBe(true);
  });

  it('rejects payment before return', () => {
    const group = form({
      departureDate: '2026-10-23',
      returnDate: '2026-10-24',
      txnDate: '2026-10-23'
    });
    expect(group.hasError('paymentBeforeTrip')).toBe(true);
  });
});
