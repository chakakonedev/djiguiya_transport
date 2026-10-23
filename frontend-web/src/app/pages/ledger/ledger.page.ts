import { Component, DestroyRef, computed, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators
} from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { switchMap } from 'rxjs/operators';
import { AuthService } from '../../core/auth.service';
import { TransactionService } from '../../core/transaction.service';
import { TripService } from '../../core/trip.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';
import { LedgerTransaction, StaffMember, Vehicle } from '../../core/api.types';
import {
  EXPENSE_GROUPS,
  EXPENSE_SUBGROUP_TONES,
  ExpenseGroup,
  OTHER_SUBGROUP,
  SUBGROUPS_BY_GROUP
} from '../../core/expense-catalog';
import { formatXof } from '../../core/format.util';
import { canEditLedger } from '../../core/roles.util';

type LedgerTab = 'revenue' | 'expense';

const DEFAULT_REVENUE_LABEL = 'Versement voyage';
const DEFAULT_EXPENSE_LABEL = 'Dépense';

/** departure ≤ return, and payment date ≥ both trip dates. */
export function revenueTripDatesValidator(): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    const departure = group.get('departureDate')?.value as string | null;
    const ret = group.get('returnDate')?.value as string | null;
    const txnDate = group.get('txnDate')?.value as string | null;
    if (!departure || !ret || !txnDate) return null;

    if (departure > ret) {
      return { departureAfterReturn: true };
    }
    if (txnDate < departure || txnDate < ret) {
      return { paymentBeforeTrip: true };
    }
    return null;
  };
}

/**
 * Ledger: tabbed revenue (trip + payment) and expense entry, role-gated edit/delete.
 * State: `tab`, `transactions`, `editingId`, `editingTripId`, `canWrite`;
 * forms: `revenueForm` (cross-field trip date validator), `expenseForm` (catalog-driven).
 */
@Component({
  selector: 'app-ledger-page',
  standalone: true,
  imports: [ReactiveFormsModule, TranslatePipe, RouterLink],
  templateUrl: './ledger.page.html',
  styleUrl: './ledger.page.scss'
})
export class LedgerPage implements OnInit {
  private readonly txnApi = inject(TransactionService);
  private readonly tripsApi = inject(TripService);
  private readonly vehiclesApi = inject(VehicleService);
  private readonly staffApi = inject(StaffService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly tab = signal<LedgerTab>('revenue');
  readonly transactions = signal<LedgerTransaction[]>([]);
  readonly vehicles = signal<Vehicle[]>([]);
  readonly drivers = signal<StaffMember[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly editingTripId = signal<string | null>(null);
  readonly expenseGroups = EXPENSE_GROUPS;
  readonly subgroupsByGroup = SUBGROUPS_BY_GROUP;
  readonly otherSubgroup = OTHER_SUBGROUP;
  readonly formatXof = formatXof;
  readonly otherSubgroupSelected = signal(false);

  readonly canWrite = computed(() => canEditLedger(this.auth.user()?.roleCode));

  readonly revenueForm = this.fb.nonNullable.group(
    {
      vehicleId: ['', Validators.required],
      driverId: ['', Validators.required],
      departureDate: [this.today(), Validators.required],
      returnDate: [this.today(), Validators.required],
      txnDate: [this.today(), Validators.required],
      amount: [null as number | null, [Validators.required, Validators.min(1)]],
      label: ['']
    },
    { validators: revenueTripDatesValidator() }
  );

  readonly expenseForm = this.fb.nonNullable.group({
    txnDate: [this.today(), Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
    label: [''],
    categoryCode: ['bus' as ExpenseGroup, Validators.required],
    subcategoryCode: ['fuel', Validators.required],
    notes: ['']
  });

  ngOnInit(): void {
    this.vehiclesApi.list().subscribe({
      next: (list) => this.vehicles.set(list.filter((v) => v.status === 'active'))
    });
    this.staffApi.list().subscribe({
      next: (list) =>
        this.drivers.set(
          list.filter(
            (s) =>
              s.status === 'active' &&
              (s.staffType === 'driver' || s.staffType === 'driver_manager')
          )
        )
    });
    this.revenueForm.controls.vehicleId.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((vehicleId) => {
        if (!this.editingId()) {
          this.applyPrimaryDriver(vehicleId);
        }
      });
    this.revenueForm.controls.returnDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((returnDate) => {
        if (returnDate && !this.editingId()) {
          this.revenueForm.controls.txnDate.setValue(returnDate);
        }
      });
    this.expenseForm.controls.subcategoryCode.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((code) => this.applyOtherSubgroupValidators(code));
    this.applyOtherSubgroupValidators(this.expenseForm.controls.subcategoryCode.value);
    this.reload();
  }

  setTab(next: LedgerTab): void {
    if (this.editingId()) {
      this.cancelEdit();
    }
    this.tab.set(next);
  }

  expenseSubgroups(): readonly string[] {
    const group = this.expenseForm.controls.categoryCode.value as ExpenseGroup;
    return this.subgroupsByGroup[group] ?? [];
  }

  /**
   * Type badge tone: green for revenue; expense shades follow subcategory
   * (falls back to category group, then "other").
   */
  typeToneClass(row: LedgerTransaction): string {
    if (row.type === 'revenue') {
      return 'type-tone type-tone--revenue';
    }
    const sub = row.subcategoryCode?.trim();
    if (sub && EXPENSE_SUBGROUP_TONES.has(sub)) {
      return `type-tone type-tone--expense type-tone--${sub}`;
    }
    const group = (row.categoryCode as ExpenseGroup) || 'other';
    const known = EXPENSE_GROUPS.includes(group) ? group : 'other';
    return `type-tone type-tone--expense type-tone--${known}`;
  }

  amountToneClass(row: LedgerTransaction): string {
    return row.type === 'revenue' ? 'amount-tone amount-tone--revenue' : 'amount-tone amount-tone--expense';
  }

  onCategoryChange(): void {
    const subs = this.expenseSubgroups();
    if (subs.length) {
      this.expenseForm.controls.subcategoryCode.setValue(subs[0]);
    }
  }

  reload(): void {
    this.loading.set(true);
    this.txnApi.list().subscribe({
      next: (list) => {
        this.transactions.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  startEdit(row: LedgerTransaction): void {
    if (!this.canWrite()) return;
    this.editingId.set(row.id);
    this.error.set(false);

    if (row.type === 'revenue') {
      this.tab.set('revenue');
      this.editingTripId.set(row.tripId ?? null);
      if (!row.tripId) {
        this.error.set(true);
        return;
      }
      this.tripsApi.get(row.tripId).subscribe({
        next: (trip) => {
          this.revenueForm.patchValue({
            vehicleId: row.vehicleId ?? trip.vehicleId,
            driverId: row.driverId ?? trip.driverId,
            departureDate: trip.departureDate,
            returnDate: trip.returnDate,
            txnDate: row.txnDate,
            amount: row.amount,
            label: row.label === DEFAULT_REVENUE_LABEL ? '' : row.label
          });
        },
        error: () => this.error.set(true)
      });
      return;
    }

    this.tab.set('expense');
    this.editingTripId.set(null);
    const category = (row.categoryCode as ExpenseGroup) || 'bus';
    const subcategory = row.subcategoryCode || this.subgroupsByGroup[category]?.[0] || 'fuel';
    this.expenseForm.patchValue({
      txnDate: row.txnDate,
      amount: row.amount,
      label: row.label === DEFAULT_EXPENSE_LABEL ? '' : row.label,
      categoryCode: category,
      subcategoryCode: subcategory,
      notes: row.notes ?? ''
    });
    this.applyOtherSubgroupValidators(subcategory);
  }

  cancelEdit(): void {
    this.editingId.set(null);
    this.editingTripId.set(null);
    this.resetRevenueForm();
    this.resetExpenseForm();
  }

  remove(row: LedgerTransaction): void {
    if (!this.canWrite()) return;
    if (!confirm('Supprimer cette écriture ?')) return;
    this.saving.set(true);
    this.error.set(false);
    this.txnApi.remove(row.id).subscribe({
      next: () => {
        if (this.editingId() === row.id) {
          this.cancelEdit();
        }
        this.saving.set(false);
        this.reload();
      },
      error: () => {
        this.saving.set(false);
        this.error.set(true);
      }
    });
  }

  submitRevenue(): void {
    if (!this.canWrite() || this.revenueForm.invalid) return;
    const raw = this.revenueForm.getRawValue();
    this.saving.set(true);
    this.error.set(false);
    const label = raw.label.trim() || DEFAULT_REVENUE_LABEL;
    const editingId = this.editingId();
    const tripId = this.editingTripId();

    if (editingId && tripId) {
      this.tripsApi
        .update(tripId, {
          vehicleId: raw.vehicleId,
          departureDate: raw.departureDate,
          returnDate: raw.returnDate,
          driverId: raw.driverId
        })
        .pipe(
          switchMap(() =>
            this.txnApi.update(editingId, {
              type: 'revenue',
              txnDate: raw.txnDate,
              amount: raw.amount!,
              label,
              tripId,
              vehicleId: raw.vehicleId,
              driverId: raw.driverId
            })
          )
        )
        .subscribe({
          next: () => {
            this.saving.set(false);
            this.cancelEdit();
            this.reload();
          },
          error: () => {
            this.saving.set(false);
            this.error.set(true);
          }
        });
      return;
    }

    this.tripsApi
      .create({
        vehicleId: raw.vehicleId,
        departureDate: raw.departureDate,
        returnDate: raw.returnDate,
        driverId: raw.driverId
      })
      .pipe(
        switchMap((trip) =>
          this.txnApi.create({
            type: 'revenue',
            txnDate: raw.txnDate,
            amount: raw.amount!,
            label,
            tripId: trip.id,
            vehicleId: trip.vehicleId,
            driverId: trip.driverId
          })
        )
      )
      .subscribe({
        next: () => {
          this.resetRevenueForm();
          this.saving.set(false);
          this.reload();
        },
        error: () => {
          this.saving.set(false);
          this.error.set(true);
        }
      });
  }

  submitExpense(): void {
    if (!this.canWrite() || this.expenseForm.invalid) return;
    const raw = this.expenseForm.getRawValue();
    const isOther = raw.subcategoryCode === OTHER_SUBGROUP;
    const body = {
      type: 'expense' as const,
      txnDate: raw.txnDate,
      amount: raw.amount!,
      label: isOther ? raw.label.trim() : raw.label.trim() || DEFAULT_EXPENSE_LABEL,
      categoryCode: raw.categoryCode,
      subcategoryCode: raw.subcategoryCode,
      notes: isOther ? raw.notes.trim() : null
    };
    const editingId = this.editingId();
    this.saving.set(true);
    this.error.set(false);
    const req$ = editingId ? this.txnApi.update(editingId, body) : this.txnApi.create(body);
    req$.subscribe({
      next: () => {
        this.saving.set(false);
        this.cancelEdit();
        this.reload();
      },
      error: () => {
        this.saving.set(false);
        this.error.set(true);
      }
    });
  }

  private applyOtherSubgroupValidators(subcategoryCode: string): void {
    const isOther = subcategoryCode === OTHER_SUBGROUP;
    this.otherSubgroupSelected.set(isOther);
    const label = this.expenseForm.controls.label;
    const notes = this.expenseForm.controls.notes;
    if (isOther) {
      label.setValidators([Validators.required, Validators.minLength(2)]);
      notes.setValidators([Validators.required, Validators.minLength(5)]);
    } else {
      label.clearValidators();
      notes.clearValidators();
      if (!this.editingId()) {
        notes.setValue('');
      }
    }
    label.updateValueAndValidity({ emitEvent: false });
    notes.updateValueAndValidity({ emitEvent: false });
  }

  private applyPrimaryDriver(vehicleId: string): void {
    const vehicle = this.vehicles().find((v) => v.id === vehicleId);
    this.revenueForm.controls.driverId.setValue(vehicle?.primaryDriverId ?? '');
  }

  private resetRevenueForm(): void {
    const vehicleId = this.revenueForm.controls.vehicleId.value;
    this.revenueForm.patchValue({
      amount: null,
      label: '',
      departureDate: this.today(),
      returnDate: this.today(),
      txnDate: this.today()
    });
    this.applyPrimaryDriver(vehicleId);
  }

  private resetExpenseForm(): void {
    this.expenseForm.patchValue({ amount: null, label: '', notes: '' });
    this.applyOtherSubgroupValidators(this.expenseForm.controls.subcategoryCode.value);
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
