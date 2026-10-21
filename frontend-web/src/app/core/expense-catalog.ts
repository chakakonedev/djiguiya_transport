export const EXPENSE_GROUPS = ['bus', 'family', 'baba', 'other'] as const;

export type ExpenseGroup = (typeof EXPENSE_GROUPS)[number];

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
    'vignette'
  ],
  family: ['condiments', 'electricity', 'household', 'tontine', 'medical_family'],
  baba: ['baba_money', 'baba_clothes', 'baba_tontine'],
  other: ['credit', 'misc']
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
