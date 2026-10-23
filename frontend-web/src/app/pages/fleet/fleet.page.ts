import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/auth.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';
import { StaffMember, Vehicle, VehicleDocument } from '../../core/api.types';
import { VEHICLE_DOC_TYPES } from '../../core/expense-catalog';
import { formatXof, localIsoDate } from '../../core/format.util';
import { canManageFleet } from '../../core/roles.util';

/**
 * Fleet management: register vehicles, assign primary drivers, track renewal documents.
 * State: `vehicles`, `drivers`, `expandedId`, `canManage`; forms: `createForm`, per-vehicle forms.
 */
@Component({
  selector: 'app-fleet-page',
  standalone: true,
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './fleet.page.html',
  styleUrl: './fleet.page.scss'
})
export class FleetPage implements OnInit {
  private readonly vehiclesApi = inject(VehicleService);
  private readonly staffApi = inject(StaffService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly vehicles = signal<Vehicle[]>([]);
  readonly drivers = signal<StaffMember[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly loadError = signal(false);
  readonly actionError = signal(false);
  readonly expandedId = signal<string | null>(null);
  readonly docTypes = VEHICLE_DOC_TYPES;
  readonly formatXof = formatXof;
  readonly canManage = computed(() => canManageFleet(this.auth.user()?.roleCode));

  readonly createForm = this.fb.nonNullable.group({
    name: ['', Validators.required],
    plate: [''],
    purchasePrice: [null as number | null],
    purchaseDate: ['']
  });

  readonly driverForms = new Map<string, ReturnType<FormBuilder['group']>>();
  readonly docForms = new Map<string, ReturnType<FormBuilder['group']>>();

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.loadError.set(false);
    this.actionError.set(false);
    this.vehiclesApi.list().subscribe({
      next: (list) => {
        this.vehicles.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.loadError.set(true);
        this.loading.set(false);
      }
    });
    this.staffApi.list().subscribe({
      next: (list) =>
        this.drivers.set(list.filter((s) => s.staffType === 'driver' || s.staffType === 'driver_manager'))
    });
  }

  submitCreate(): void {
    if (!this.canManage() || this.createForm.invalid) return;
    this.saving.set(true);
    this.actionError.set(false);
    const raw = this.createForm.getRawValue();
    this.vehiclesApi
      .create({
        name: raw.name,
        plate: raw.plate || null,
        purchasePrice: raw.purchasePrice,
        purchaseDate: raw.purchaseDate || null
      })
      .subscribe({
        next: () => {
          this.createForm.reset({ name: '', plate: '', purchasePrice: null, purchaseDate: '' });
          this.saving.set(false);
          this.reload();
        },
        error: () => {
          this.saving.set(false);
          this.actionError.set(true);
        }
      });
  }

  driverDisplayName(vehicle: Vehicle): string | null {
    if (vehicle.primaryDriverName) return vehicle.primaryDriverName;
    if (!vehicle.primaryDriverId) return null;
    return this.drivers().find((d) => d.id === vehicle.primaryDriverId)?.fullName ?? null;
  }

  toggleExpand(vehicle: Vehicle): void {
    const next = this.expandedId() === vehicle.id ? null : vehicle.id;
    this.expandedId.set(next);
    if (next) {
      this.ensureDriverForm(vehicle);
      this.loadDocuments(vehicle.id);
    }
  }

  ensureDriverForm(vehicle: Vehicle): void {
    if (this.driverForms.has(vehicle.id)) return;
    this.driverForms.set(
      vehicle.id,
      this.fb.nonNullable.group({
        driverId: [vehicle.primaryDriverId ?? '', Validators.required],
        startDate: [localIsoDate(), Validators.required]
      })
    );
  }

  driverForm(vehicleId: string) {
    return this.driverForms.get(vehicleId)!;
  }

  savePrimaryDriver(vehicleId: string): void {
    if (!this.canManage()) return;
    const form = this.driverForm(vehicleId);
    if (!form || form.invalid) return;
    this.saving.set(true);
    this.actionError.set(false);
    const { driverId, startDate } = form.getRawValue() as { driverId: string; startDate: string };
    this.vehiclesApi
      .setPrimaryDriver(vehicleId, { driverId, startDate })
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.reload();
        },
        error: () => {
          this.saving.set(false);
          this.actionError.set(true);
        }
      });
  }

  loadDocuments(vehicleId: string): void {
    this.vehiclesApi.getDocuments(vehicleId).subscribe({
      next: (docs) => this.syncDocForm(vehicleId, docs),
      error: () => this.syncDocForm(vehicleId, [])
    });
  }

  syncDocForm(vehicleId: string, docs: VehicleDocument[]): void {
    const byType = new Map(docs.map((d) => [d.docType, d]));
    const group = this.fb.group({});
    for (const docType of this.docTypes) {
      const existing = byType.get(docType);
      group.addControl(
        docType,
        this.fb.group({
          expiresOn: [existing?.expiresOn ?? ''],
          lastRenewedOn: [existing?.lastRenewedOn ?? ''],
          renewalPeriodMonths: [existing?.renewalPeriodMonths ?? 12],
          lastAmount: [existing?.lastAmount ?? null as number | null]
        })
      );
    }
    this.docForms.set(vehicleId, group);
  }

  docForm(vehicleId: string) {
    return this.docForms.get(vehicleId);
  }

  saveDocuments(vehicleId: string): void {
    if (!this.canManage()) return;
    const form = this.docForm(vehicleId);
    if (!form) return;
    const payload: VehicleDocument[] = [];
    for (const docType of this.docTypes) {
      const row = form.get(docType)?.getRawValue() as {
        expiresOn: string;
        lastRenewedOn: string;
        renewalPeriodMonths: number;
        lastAmount: number | null;
      };
      if (!row?.expiresOn) continue;
      payload.push({
        docType,
        expiresOn: row.expiresOn,
        lastRenewedOn: row.lastRenewedOn || null,
        renewalPeriodMonths: row.renewalPeriodMonths,
        lastAmount: row.lastAmount
      });
    }

    this.saving.set(true);
    this.actionError.set(false);
    this.vehiclesApi.saveDocuments(vehicleId, payload).subscribe({
      next: () => this.saving.set(false),
      error: () => {
        this.saving.set(false);
        this.actionError.set(true);
      }
    });
  }
}
