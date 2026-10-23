/**
 * Role-based UI permissions derived from the logged-in user's roleCode.
 * Used to gate ledger edits and staff management without server round-trips.
 */

/** Role codes from JWT / login response (lowercase). */
export const RoleCodes = {
  OWNER: 'owner',
  CASHIER: 'cashier',
  ACCOUNTANT: 'accountant',
  CASHIER_ACCOUNTANT: 'cashier_accountant',
  DRIVER_MANAGER: 'driver_manager'
} as const;

export type RoleCode = (typeof RoleCodes)[keyof typeof RoleCodes] | string;

export function isOwner(roleCode: string | null | undefined): boolean {
  return roleCode === RoleCodes.OWNER;
}

/** PDG, caissier, comptable — can create/edit/delete ledger entries. */
export function canEditLedger(roleCode: string | null | undefined): boolean {
  return (
    roleCode === RoleCodes.OWNER ||
    roleCode === RoleCodes.CASHIER ||
    roleCode === RoleCodes.ACCOUNTANT ||
    roleCode === RoleCodes.CASHIER_ACCOUNTANT
  );
}

export function canManageStaff(roleCode: string | null | undefined): boolean {
  return isOwner(roleCode);
}
