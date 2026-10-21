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

export const STAFF_TYPES = [
  'driver',
  'driver_manager',
  'cashier',
  'accountant',
  'owner',
  'baba_beneficiary',
  'other'
] as const;

export const VEHICLE_DOC_TYPES = ['insurance', 'inspection', 'vignette'] as const;
