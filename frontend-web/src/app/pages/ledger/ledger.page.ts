import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
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
import { TransactionService } from '../../core/transaction.service';
import { TripService } from '../../core/trip.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';
import { LedgerTransaction, StaffMember, Vehicle } from '../../core/api.types';
import {
  EXPENSE_GROUPS,
  ExpenseGroup,
  SUBGROUPS_BY_GROUP
} from '../../core/expense-catalog';
import { formatXof } from '../../core/format.util';

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
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly tab = signal<LedgerTab>('revenue');
  readonly transactions = signal<LedgerTransaction[]>([]);
  readonly vehicles = signal<Vehicle[]>([]);
  readonly drivers = signal<StaffMember[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal(false);
  readonly expenseGroups = EXPENSE_GROUPS;
  readonly subgroupsByGroup = SUBGROUPS_BY_GROUP;
  readonly formatXof = formatXof;

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
    subcategoryCode: ['fuel', Validators.required]
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
      .subscribe((vehicleId) => this.applyPrimaryDriver(vehicleId));
    this.revenueForm.controls.returnDate.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((returnDate) => {
        if (returnDate) {
          this.revenueForm.controls.txnDate.setValue(returnDate);
        }
      });
    this.reload();
  }

  setTab(next: LedgerTab): void {
    this.tab.set(next);
  }

  expenseSubgroups(): readonly string[] {
    const group = this.expenseForm.controls.categoryCode.value as ExpenseGroup;
    return this.subgroupsByGroup[group] ?? [];
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

  submitRevenue(): void {
    if (this.revenueForm.invalid) return;
    const raw = this.revenueForm.getRawValue();
    this.saving.set(true);
    this.error.set(false);

    const label = raw.label.trim() || DEFAULT_REVENUE_LABEL;

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
          const vehicleId = this.revenueForm.controls.vehicleId.value;
          this.revenueForm.patchValue({
            amount: null,
            label: '',
            departureDate: this.today(),
            returnDate: this.today(),
            txnDate: this.today()
          });
          this.applyPrimaryDriver(vehicleId);
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
    if (this.expenseForm.invalid) return;
    const raw = this.expenseForm.getRawValue();
    this.saveTxn({
      type: 'expense',
      txnDate: raw.txnDate,
      amount: raw.amount!,
      label: raw.label.trim() || DEFAULT_EXPENSE_LABEL,
      categoryCode: raw.categoryCode,
      subcategoryCode: raw.subcategoryCode
    });
  }

  private applyPrimaryDriver(vehicleId: string): void {
    const vehicle = this.vehicles().find((v) => v.id === vehicleId);
    this.revenueForm.controls.driverId.setValue(vehicle?.primaryDriverId ?? '');
  }

  private saveTxn(body: Parameters<TransactionService['create']>[0]): void {
    this.saving.set(true);
    this.error.set(false);
    this.txnApi.create(body).subscribe({
      next: () => {
        this.expenseForm.patchValue({ amount: null, label: '' });
        this.saving.set(false);
        this.reload();
      },
      error: () => {
        this.saving.set(false);
        this.error.set(true);
      }
    });
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
