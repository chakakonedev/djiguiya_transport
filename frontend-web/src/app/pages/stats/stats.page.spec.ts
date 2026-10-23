import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { StatsPage } from './stats.page';
import { StatsService } from '../../core/stats.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';

/**
 * StatsPage: summary + monthly series, category/subgroup presentation helpers,
 * and month filter refresh behavior.
 */
describe('StatsPage', () => {
  const statsApi = {
    summary: vi.fn(),
    monthly: vi.fn()
  };
  const vehiclesApi = { list: vi.fn(() => of([])) };
  const staffApi = {
    list: vi.fn(() => of([{ id: 'd1', fullName: 'Driver', staffType: 'driver' }]))
  };

  beforeEach(async () => {
    statsApi.summary.mockReset();
    statsApi.monthly.mockReset();
    await TestBed.configureTestingModule({
      imports: [StatsPage],
      providers: [
        provideTranslateService(),
        { provide: StatsService, useValue: statsApi },
        { provide: VehicleService, useValue: vehiclesApi },
        { provide: StaffService, useValue: staffApi }
      ]
    }).compileComponents();
  });

  /** Loads KPIs, splits category vs subgroup rows, and builds chart helpers. */
  it('loads summary and monthly series', async () => {
    statsApi.summary.mockReturnValue(
      of({
        totalRevenue: 100,
        totalExpense: 40,
        netProfit: 60,
        groups: [
          { categoryCode: 'bus', subcategoryCode: null, amount: 40 },
          { categoryCode: 'bus', subcategoryCode: 'fuel', amount: 40 }
        ]
      })
    );
    statsApi.monthly.mockReturnValue(
      of({
        fiscalStartYear: 2025,
        months: [{ monthKey: '2025-08', totalRevenue: 10, totalExpense: 2, netProfit: 8 }]
      })
    );

    const fixture = TestBed.createComponent(StatsPage);
    fixture.detectChanges();
    await fixture.whenStable();

    const page = fixture.componentInstance;
    expect(page.summary()?.netProfit).toBe(60);
    expect(page.categoryGroups().length).toBe(1);
    expect(page.subgroupRows().length).toBe(1);
    expect(page.groupColor('bus')).toBe('#0f766e');
    expect(page.barPct(20)).toBeGreaterThan(0);
    expect(page.monthPoints().length).toBe(1);
    expect(page.loading()).toBe(false);
  });

  /** Monthly API failure sets the page error flag. */
  it('sets error when monthly fails', async () => {
    statsApi.summary.mockReturnValue(of({ totalRevenue: 0, groups: [] }));
    statsApi.monthly.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(StatsPage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toBe(true);
  });

  /** Changing month reloads summary; label helpers cover group/subgroup/unknown. */
  it('selectMonth refreshes and helpers work', async () => {
    statsApi.summary.mockReturnValue(
      of({ totalRevenue: 0, totalExpense: 0, netProfit: 0, groups: [] })
    );
    statsApi.monthly.mockReturnValue(of({ fiscalStartYear: 2025, months: [] }));
    const fixture = TestBed.createComponent(StatsPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;

    page.selectMonth(8);
    expect(page.filters.controls.month.value).toBe(8);
    expect(statsApi.summary.mock.calls.length).toBeGreaterThan(1);
    expect(page.groupLabel({ categoryCode: 'bus', subcategoryCode: null, amount: 0 })).toContain('groups.bus');
    expect(page.groupLabel({ categoryCode: 'bus', subcategoryCode: 'fuel', amount: 1 })).toContain('subgroups.fuel');
    expect(page.groupLabel({ categoryCode: null, subcategoryCode: null, amount: 0 })).toBe('stats.unknownGroup');
    expect(page.orderedCategoryGroups().length).toBe(4);
    expect(page.fiscalLabel().start).toMatch(/-08$/);
    expect(page.histPct(0)).toBe(2);
  });
});
