import { describe, expect, it } from 'vitest';
import { canEditLedger, canManageStaff, isOwner, RoleCodes } from './roles.util';

describe('roles.util', () => {
  it('isOwner', () => {
    expect(isOwner(RoleCodes.OWNER)).toBe(true);
    expect(isOwner(RoleCodes.CASHIER)).toBe(false);
    expect(isOwner(null)).toBe(false);
  });

  it('canEditLedger for finance roles', () => {
    expect(canEditLedger(RoleCodes.OWNER)).toBe(true);
    expect(canEditLedger(RoleCodes.CASHIER)).toBe(true);
    expect(canEditLedger(RoleCodes.ACCOUNTANT)).toBe(true);
    expect(canEditLedger(RoleCodes.CASHIER_ACCOUNTANT)).toBe(true);
    expect(canEditLedger(RoleCodes.DRIVER_MANAGER)).toBe(false);
  });

  it('canManageStaff only owner', () => {
    expect(canManageStaff(RoleCodes.OWNER)).toBe(true);
    expect(canManageStaff(RoleCodes.CASHIER)).toBe(false);
  });
});
