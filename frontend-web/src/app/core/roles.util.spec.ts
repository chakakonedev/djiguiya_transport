import { describe, expect, it } from 'vitest';
import { canEditLedger, canManageFleet, canManageStaff, isOwner, RoleCodes } from './roles.util';

/**
 * Role helpers: owner detection and coarse permissions for ledger, staff, and fleet.
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

  /** Fleet mutations for owner and driver manager only. */
  it('canManageFleet for owner and driver manager', () => {
    expect(canManageFleet(RoleCodes.OWNER)).toBe(true);
    expect(canManageFleet(RoleCodes.DRIVER_MANAGER)).toBe(true);
    expect(canManageFleet(RoleCodes.CASHIER)).toBe(false);
    expect(canManageFleet(RoleCodes.ACCOUNTANT)).toBe(false);
  });
});
