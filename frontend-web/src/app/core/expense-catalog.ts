export const EXPENSE_GROUPS = ['bus', 'family', 'baba', 'other'] as const;

export type ExpenseGroup = (typeof EXPENSE_GROUPS)[number];

/** Custom subcategory: requires a free-text label and explanation. */
export const OTHER_SUBGROUP = 'other';

export const SUBGROUPS_BY_GROUP: Record<ExpenseGroup, readonly string[]> = {
  bus: [
    'fuel',
    'oil_service',
    'parts_brakes',
    'tires',
    'mechanic',
    'ac_system',
    'inspection',
    'insurance',
    'cleaning',
    'driver_salary',
    'driver_bonus',
    'vignette',
    OTHER_SUBGROUP
  ],
  family: [
    'condiments',
    'electricity',
    'household',
    'tontine',
    'medical_family',
    OTHER_SUBGROUP
  ],
  baba: ['baba_money', 'baba_clothes', 'baba_tontine', OTHER_SUBGROUP],
  other: ['credit', 'misc', OTHER_SUBGROUP]
};

/** Known subcategory codes used for ledger type-badge color tones. */
export const EXPENSE_SUBGROUP_TONES: ReadonlySet<string> = new Set(
  Object.values(SUBGROUPS_BY_GROUP).flat()
);

export const STAFF_TYPES = [
  'driver',
  'driver_manager',
  'cashier',
  'accountant',
  'cashier_accountant',
  'owner',
  'baba_beneficiary',
  'other'
] as const;

export const VEHICLE_DOC_TYPES = ['insurance', 'inspection', 'vignette'] as const;
