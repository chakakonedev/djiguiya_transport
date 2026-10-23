import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { StatsService } from '../../core/stats.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';
import {
  StaffMember,
  StatsGroupRow,
  StatsMonthPoint,
  StatsMonthlySeries,
  StatsSummary,
  Vehicle
} from '../../core/api.types';
import { formatXof } from '../../core/format.util';
import { EXPENSE_GROUPS } from '../../core/expense-catalog';
import {
  calendarYearForFiscalMonth,
  FISCAL_MONTHS,
  fiscalPeriodLabel,
  fiscalStartYearContaining,
  monthI18nKey,
  monthShortI18nKey
} from '../../core/fiscal-year.util';

const GROUP_COLORS: Record<string, string> = {
  bus: '#0f766e',
  family: '#b45309',
  baba: '#7c3aed',
  other: '#64748b'
};

@Component({
  selector: 'app-stats-page',
  standalone: true,
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './stats.page.html',
  styleUrl: './stats.page.scss'
})
export class StatsPage implements OnInit {
  private readonly statsApi = inject(StatsService);
  private readonly vehiclesApi = inject(VehicleService);
  private readonly staffApi = inject(StaffService);
  private readonly fb = inject(FormBuilder);

  readonly vehicles = signal<Vehicle[]>([]);
  readonly drivers = signal<StaffMember[]>([]);
  readonly summary = signal<StatsSummary | null>(null);
  readonly monthly = signal<StatsMonthlySeries | null>(null);
  readonly loading = signal(false);
  readonly error = signal(false);
  readonly fiscalMonths = FISCAL_MONTHS;
  readonly formatXof = formatXof;
  readonly monthI18nKey = monthI18nKey;
  readonly monthShortI18nKey = monthShortI18nKey;

  readonly filters = this.fb.nonNullable.group({
    vehicleId: [''],
    driverId: [''],
    year: [fiscalStartYearContaining()],
    month: [null as number | null]
  });

  readonly categoryGroups = computed(() =>
    (this.summary()?.groups ?? []).filter((g) => !g.subcategoryCode)
  );

  readonly subgroupRows = computed(() =>
    (this.summary()?.groups ?? []).filter((g) => !!g.subcategoryCode)
  );

  readonly maxGroupAmount = computed(() =>
    Math.max(...this.categoryGroups().map((g) => g.amount), 1)
  );

  readonly maxMonthlyValue = computed(() => {
    const months = this.monthly()?.months ?? [];
    return Math.max(
      ...months.flatMap((m) => [m.totalRevenue, m.totalExpense, Math.abs(m.netProfit)]),
      1
    );
  });

  ngOnInit(): void {
    this.vehiclesApi.list().subscribe({ next: (v) => this.vehicles.set(v) });
    this.staffApi.list().subscribe({
      next: (s) =>
        this.drivers.set(
          s.filter((x) => x.staffType === 'driver' || x.staffType === 'driver_manager')
        )
    });
    this.refreshAll();
  }

  selectMonth(month: number | null): void {
    this.filters.controls.month.setValue(month);
    this.refreshAll();
  }

  refreshAll(): void {
    this.loading.set(true);
    this.error.set(false);
    const f = this.filters.getRawValue();
    const fiscalYear = f.year;
    const base = {
      vehicleId: f.vehicleId || undefined,
      driverId: f.driverId || undefined
    };

    const summaryFilters =
      f.month != null
        ? {
            ...base,
            month: f.month,
            year: calendarYearForFiscalMonth(fiscalYear, f.month)
          }
        : { ...base, year: fiscalYear };

    this.statsApi.summary(summaryFilters).subscribe({
      next: (data) => this.summary.set(data),
      error: () => this.error.set(true)
    });

    this.statsApi.monthly(fiscalYear, base).subscribe({
      next: (data) => {
        this.monthly.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  fiscalLabel(): { start: string; end: string } {
    return fiscalPeriodLabel(this.filters.controls.year.value);
  }

  groupLabel(row: StatsGroupRow): string {
    if (row.subcategoryCode) {
      return `catalog.subgroups.${row.subcategoryCode}`;
    }
    if (row.categoryCode) {
      return `catalog.groups.${row.categoryCode}`;
    }
    return 'stats.unknownGroup';
  }

  groupColor(categoryCode: string | null | undefined): string {
    return GROUP_COLORS[categoryCode ?? 'other'] ?? GROUP_COLORS['other'];
  }

  barPct(amount: number): number {
    return Math.round((amount / this.maxGroupAmount()) * 100);
  }

  histPct(value: number): number {
    return Math.max(2, Math.round((Math.abs(value) / this.maxMonthlyValue()) * 100));
  }

  orderedCategoryGroups(): StatsGroupRow[] {
    const byCode = new Map(this.categoryGroups().map((g) => [g.categoryCode, g]));
    return EXPENSE_GROUPS.map(
      (code) => byCode.get(code) ?? { categoryCode: code, subcategoryCode: null, amount: 0 }
    );
  }

  monthPoints(): StatsMonthPoint[] {
    return this.monthly()?.months ?? [];
  }
}
