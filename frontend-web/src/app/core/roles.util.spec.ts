import { describe, expect, it } from 'vitest';
import { canEditLedger, canManageStaff, isOwner, RoleCodes } from './roles.util';

/**
 * Role helpers: owner detection and coarse permissions for ledger edits and staff management.
 */
describe('roles.util', () => {
  /** isOwner is true only for OWNER; false for other roles and null. */
  it('isOwner', () => {
    expect(isOwner(RoleCodes.OWNER)).toBe(true);
    expect(isOwner(RoleCodes.CASHIER)).toBe(false);
    expect(isOwner(null)).toBe(false);
  });

  /** Ledger write access for owner and finance roles; not driver manager. */
  it('canEditLedger for finance roles', () => {
    expect(canEditLedger(RoleCodes.OWNER)).toBe(true);
    expect(canEditLedger(RoleCodes.CASHIER)).toBe(true);
    expect(canEditLedger(RoleCodes.ACCOUNTANT)).toBe(true);
    expect(canEditLedger(RoleCodes.CASHIER_ACCOUNTANT)).toBe(true);
    expect(canEditLedger(RoleCodes.DRIVER_MANAGER)).toBe(false);
  });

  /** Staff CRUD restricted to owner role. */
  it('canManageStaff only owner', () => {
    expect(canManageStaff(RoleCodes.OWNER)).toBe(true);
    expect(canManageStaff(RoleCodes.CASHIER)).toBe(false);
  });
});
