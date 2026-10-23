import { describe, expect, it } from 'vitest';
import {
  EXPENSE_GROUPS,
  EXPENSE_SUBGROUP_TONES,
  OTHER_SUBGROUP,
  STAFF_TYPES,
  SUBGROUPS_BY_GROUP,
  VEHICLE_DOC_TYPES
} from './expense-catalog';

describe('expense-catalog', () => {
  it('defines groups and other subgroup', () => {
    expect(EXPENSE_GROUPS).toContain('bus');
    expect(SUBGROUPS_BY_GROUP.bus).toContain(OTHER_SUBGROUP);
    expect(EXPENSE_SUBGROUP_TONES.has('fuel')).toBe(true);
    expect(STAFF_TYPES).toContain('driver');
    expect(VEHICLE_DOC_TYPES).toEqual(['insurance', 'inspection', 'vignette']);
  });
});
