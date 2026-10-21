import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { TransactionService } from '../../core/transaction.service';
import { TripService } from '../../core/trip.service';
import { LedgerTransaction, Trip } from '../../core/api.types';
import {
  EXPENSE_GROUPS,
  ExpenseGroup,
  SUBGROUPS_BY_GROUP
} from '../../core/expense-catalog';
import { formatXof } from '../../core/format.util';

type LedgerTab = 'revenue' | 'expense';

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
  private readonly fb = inject(FormBuilder);

  readonly tab = signal<LedgerTab>('revenue');
  readonly transactions = signal<LedgerTransaction[]>([]);
  readonly trips = signal<Trip[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal(false);
  readonly expenseGroups = EXPENSE_GROUPS;
  readonly subgroupsByGroup = SUBGROUPS_BY_GROUP;
  readonly formatXof = formatXof;

  readonly revenueForm = this.fb.nonNullable.group({
    txnDate: [this.today(), Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
    label: ['', Validators.required],
    tripId: ['', Validators.required]
  });

  readonly expenseForm = this.fb.nonNullable.group({
    txnDate: [this.today(), Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
    label: ['', Validators.required],
    categoryCode: ['bus' as ExpenseGroup, Validators.required],
    subcategoryCode: ['fuel', Validators.required]
  });

  ngOnInit(): void {
    this.tripsApi.list().subscribe({
      next: (list) => this.trips.set(list)
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
    this.saveTxn({
      type: 'revenue',
      txnDate: raw.txnDate,
      amount: raw.amount!,
      label: raw.label,
      tripId: raw.tripId
    });
  }

  submitExpense(): void {
    if (this.expenseForm.invalid) return;
    const raw = this.expenseForm.getRawValue();
    this.saveTxn({
      type: 'expense',
      txnDate: raw.txnDate,
      amount: raw.amount!,
      label: raw.label,
      categoryCode: raw.categoryCode,
      subcategoryCode: raw.subcategoryCode
    });
  }

  private saveTxn(body: Parameters<TransactionService['create']>[0]): void {
    this.saving.set(true);
    this.txnApi.create(body).subscribe({
      next: () => {
        this.saving.set(false);
        this.reload();
      },
      error: () => {
        this.saving.set(false);
        this.error.set(true);
      }
    });
  }

  tripLabel(trip: Trip): string {
    return `${trip.departureDate} → ${trip.returnDate} (${trip.monthKey})`;
  }

  private today(): string {
    return new Date().toISOString().slice(0, 10);
  }
}
