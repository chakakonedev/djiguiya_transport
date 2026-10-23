import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { StaffPage } from './staff.page';
import { StaffService } from '../../core/staff.service';
import { ProfitShareService } from '../../core/profit-share.service';
import { AuthService } from '../../core/auth.service';

/**
 * StaffPage: staff roster + profit-share rules joined into display rows;
 * owners can create/edit staff and sync share / quarterly-bonus rules.
 */
describe('StaffPage', () => {
  const staffApi = { list: vi.fn(), create: vi.fn(), update: vi.fn() };
  const profitApi = { listRules: vi.fn(), updateRule: vi.fn() };
  const auth = { user: vi.fn(() => ({ roleCode: 'owner' })) };

  beforeEach(async () => {
    staffApi.list.mockReset();
    profitApi.listRules.mockReset();
    await TestBed.configureTestingModule({
      imports: [StaffPage],
      providers: [
        provideTranslateService(),
        { provide: StaffService, useValue: staffApi },
        { provide: ProfitShareService, useValue: profitApi },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();
  });

  /** Join staff with partner share rules; owner has manage rights. */
  it('loads staff and rules', async () => {
    staffApi.list.mockReturnValue(
      of([{ id: 's1', fullName: 'Amadou', staffType: 'driver', monthlySalary: 100 }])
    );
    profitApi.listRules.mockReturnValue(
      of([
        {
          id: 'r1',
          beneficiaryStaffId: 's1',
          ruleKind: 'partner_profit_share_pct',
          ratePercent: 1,
          enabled: true
        }
      ])
    );
    const fixture = TestBed.createComponent(StaffPage);
    fixture.detectChanges();
    await fixture.whenStable();

    const page = fixture.componentInstance;
    expect(page.staffList().length).toBe(1);
    expect(page.canManage()).toBe(true);
    expect(page.rows()[0].partnerShareRule?.id).toBe('r1');
    expect(page.loading()).toBe(false);
  });

  /** Staff list failure sets the page error flag. */
  it('sets error on reload failure', async () => {
    staffApi.list.mockReturnValue(throwError(() => new Error('fail')));
    profitApi.listRules.mockReturnValue(of([]));
    const fixture = TestBed.createComponent(StaffPage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toBe(true);
  });

  /** startCreate clears editing id and defaults staff type to driver. */
  it('startCreate resets form for owner', async () => {
    staffApi.list.mockReturnValue(of([]));
    profitApi.listRules.mockReturnValue(of([]));
    const fixture = TestBed.createComponent(StaffPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;
    page.startCreate();
    expect(page.editingId()).toBeNull();
    expect(page.form.controls.staffType.value).toBe('driver');
  });

  /** Create staff then push partner-share / quarterly-bonus rule updates. */
  it('submits create and syncs share rules', async () => {
    staffApi.list.mockReturnValue(of([]));
    profitApi.listRules.mockReturnValue(
      of([
        {
          id: 'r1',
          beneficiaryStaffId: 's-new',
          ruleKind: 'partner_profit_share_pct',
          enabled: true
        },
        {
          id: 'r2',
          beneficiaryStaffId: 's-new',
          ruleKind: 'quarterly_salary_bonus',
          enabled: true
        }
      ])
    );
    staffApi.create.mockReturnValue(
      of({ id: 's-new', fullName: 'New', staffType: 'driver', status: 'active' })
    );
    profitApi.updateRule.mockReturnValue(of({ id: 'r1' }));

    const fixture = TestBed.createComponent(StaffPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;
    page.rules.set([
      {
        id: 'r1',
        beneficiaryStaffId: 's-new',
        ruleKind: 'partner_profit_share_pct',
        enabled: true,
        effectiveFrom: '2025-08-01'
      },
      {
        id: 'r2',
        beneficiaryStaffId: 's-new',
        ruleKind: 'quarterly_salary_bonus',
        enabled: true,
        effectiveFrom: '2025-08-01'
      }
    ]);
    page.form.setValue({
      fullName: 'New Driver',
      staffType: 'driver',
      monthlySalary: 100000,
      payDay: 25,
      partnerSharePercent: 2,
      quarterlyBonusMonths: 1
    });
    page.submit();
    await fixture.whenStable();

    expect(staffApi.create).toHaveBeenCalled();
    expect(profitApi.updateRule).toHaveBeenCalled();
  });

  /** Display helpers for share % and quarterly bonus amounts. */
  it('formats share and quarterly bonus', async () => {
    staffApi.list.mockReturnValue(of([]));
    profitApi.listRules.mockReturnValue(of([]));
    const fixture = TestBed.createComponent(StaffPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;
    expect(page.formatSharePercent(null)).toBe('—');
    expect(page.formatSharePercent({ id: 'r', ruleKind: 'x', ratePercent: 1.5, enabled: true, effectiveFrom: 'x' })).toBe(
      '1.5 %'
    );
    expect(page.formatQuarterlyBonus(null, 100)).toBe('—');
    expect(
      page.formatQuarterlyBonus(
        { id: 'r', ruleKind: 'x', salaryMonthsEquivalent: 2, enabled: true, effectiveFrom: 'x' },
        100000
      )
    ).toContain('2');
  });
});
