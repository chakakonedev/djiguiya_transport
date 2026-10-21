import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { StatsService } from '../../core/stats.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';
import {
  CompareMonthRow,
  StaffMember,
  StatsGroupRow,
  StatsSummary,
  Vehicle,
  YoyRow
} from '../../core/api.types';
import { formatXof } from '../../core/format.util';

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
  readonly compare = signal<CompareMonthRow | null>(null);
  readonly yoyRows = signal<YoyRow[]>([]);
  readonly loading = signal(false);
  readonly error = signal(false);
  readonly monthChips = Array.from({ length: 12 }, (_, i) => i + 1);
  readonly formatXof = formatXof;

  readonly filters = this.fb.nonNullable.group({
    vehicleId: [''],
    driverId: [''],
    year: [new Date().getFullYear()],
    month: [new Date().getMonth() + 1],
    compareMonthIndex: [new Date().getMonth() + 1]
  });

  ngOnInit(): void {
    this.vehiclesApi.list().subscribe({ next: (v) => this.vehicles.set(v) });
    this.staffApi.list().subscribe({
      next: (s) => this.drivers.set(s.filter((x) => x.staffType === 'driver'))
    });
    this.refreshAll();
  }

  selectMonth(month: number): void {
    this.filters.controls.month.setValue(month);
    this.refreshAll();
  }

  refreshAll(): void {
    this.loading.set(true);
    this.error.set(false);
    const f = this.filters.getRawValue();
    const base = {
      vehicleId: f.vehicleId || undefined,
      driverId: f.driverId || undefined,
      year: f.year
    };

    this.statsApi.summary({ ...base, month: f.month }).subscribe({
      next: (data) => this.summary.set(data),
      error: () => this.error.set(true)
    });

    this.statsApi.compareMonth(f.compareMonthIndex, base).subscribe({
      next: (data) => this.compare.set(data),
      error: () => this.error.set(true)
    });

    this.statsApi.yoy(f.year, base).subscribe({
      next: (rows) => {
        this.yoyRows.set(rows);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  groupLabel(row: StatsGroupRow): string {
    if (row.subcategoryCode) {
      return `catalog.subgroups.${row.subcategoryCode}`;
    }
    if (row.categoryCode) {
      return `catalog.groups.${row.categoryCode}`;
    }
    return row.label ?? 'stats.unknownGroup';
  }
}
