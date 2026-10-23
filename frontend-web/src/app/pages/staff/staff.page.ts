import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { forkJoin, of } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AuthService } from '../../core/auth.service';
import { StaffService } from '../../core/staff.service';
import { ProfitShareService } from '../../core/profit-share.service';
import { ProfitShareRule, StaffMember } from '../../core/api.types';
import { STAFF_TYPES } from '../../core/expense-catalog';
import { formatXof } from '../../core/format.util';
import { canManageStaff } from '../../core/roles.util';

const PARTNER_SHARE = 'partner_profit_share_pct';
const QUARTERLY_BONUS = 'quarterly_salary_bonus';

/** Table row combining staff profile with linked profit-share rules for display. */
export interface StaffRowView {
  staff: StaffMember;
  partnerShareRule: ProfitShareRule | null;
  quarterlyBonusRule: ProfitShareRule | null;
}

/**
 * Staff directory; owners can create/edit staff and sync partner share / quarterly bonus rules.
 * State: `staffList`, `rules`, `editingId`, `canManage`; form: unified create/edit `form`.
 */
@Component({
  selector: 'app-staff-page',
  standalone: true,
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './staff.page.html',
  styleUrl: './staff.page.scss'
})
export class StaffPage implements OnInit {
  private readonly staffApi = inject(StaffService);
  private readonly profitApi = inject(ProfitShareService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  readonly staffList = signal<StaffMember[]>([]);
  readonly rules = signal<ProfitShareRule[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal(false);
  readonly editingId = signal<string | null>(null);
  readonly staffTypes = STAFF_TYPES;
  readonly formatXof = formatXof;

  readonly canManage = computed(() => canManageStaff(this.auth.user()?.roleCode));

  readonly rows = computed<StaffRowView[]>(() => {
    const rules = this.rules();
    return this.staffList().map((staff) => ({
      staff,
      partnerShareRule:
        rules.find(
          (r) => r.beneficiaryStaffId === staff.id && r.ruleKind === PARTNER_SHARE && r.enabled
        ) ?? null,
      quarterlyBonusRule:
        rules.find(
          (r) => r.beneficiaryStaffId === staff.id && r.ruleKind === QUARTERLY_BONUS && r.enabled
        ) ?? null
    }));
  });

  readonly form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    staffType: ['driver', Validators.required],
    monthlySalary: [null as number | null],
    payDay: [null as number | null, [Validators.min(1), Validators.max(28)]],
    partnerSharePercent: [null as number | null, [Validators.min(0), Validators.max(100)]],
    quarterlyBonusMonths: [null as number | null, [Validators.min(0), Validators.max(12)]]
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(false);
    forkJoin({
      staff: this.staffApi.list(),
      rules: this.profitApi.listRules()
    }).subscribe({
      next: ({ staff, rules }) => {
        this.staffList.set(staff);
        this.rules.set(rules);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  startCreate(): void {
    if (!this.canManage()) return;
    this.editingId.set(null);
    this.form.reset({
      fullName: '',
      staffType: 'driver',
      monthlySalary: null,
      payDay: null,
      partnerSharePercent: null,
      quarterlyBonusMonths: null
    });
  }

  startEdit(row: StaffRowView): void {
    if (!this.canManage()) return;
    this.editingId.set(row.staff.id);
    this.form.reset({
      fullName: row.staff.fullName,
      staffType: row.staff.staffType,
      monthlySalary: row.staff.monthlySalary ?? null,
      payDay: row.staff.payDay ?? null,
      partnerSharePercent: row.partnerShareRule?.ratePercent ?? null,
      quarterlyBonusMonths: row.quarterlyBonusRule?.salaryMonthsEquivalent ?? null
    });
  }

  cancelEdit(): void {
    this.startCreate();
  }

  submit(): void {
    if (!this.canManage() || this.form.invalid) return;
    this.saving.set(true);
    this.error.set(false);
    const raw = this.form.getRawValue();
    const body = {
      fullName: raw.fullName.trim(),
      staffType: raw.staffType,
      monthlySalary: raw.monthlySalary,
      payDay: raw.payDay
    };
    const editingId = this.editingId();
    const saveStaff$ = editingId
      ? this.staffApi.update(editingId, body)
      : this.staffApi.create(body);

    saveStaff$
      .pipe(
        switchMap((saved) => {
          if (!saved) return of(null);
          return this.syncShareRules(saved.id, raw.partnerSharePercent, raw.quarterlyBonusMonths);
        })
      )
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.startCreate();
          this.reload();
        },
        error: () => {
          this.saving.set(false);
          this.error.set(true);
        }
      });
  }

  formatSharePercent(rule: ProfitShareRule | null): string {
    if (rule?.ratePercent == null) return '—';
    return `${Number(rule.ratePercent)} %`;
  }

  formatQuarterlyBonus(rule: ProfitShareRule | null, salary: number | null | undefined): string {
    if (rule?.salaryMonthsEquivalent == null) return '—';
    const n = Number(rule.salaryMonthsEquivalent);
    if (salary != null && salary > 0) {
      return `${n} × ${this.formatXof(salary)}`;
    }
    return `${n} × salaire`;
  }

  /** Updates existing profit-share rules when owner edits partner % or quarterly bonus on the form. */
  private syncShareRules(
    staffId: string,
    partnerPercent: number | null,
    bonusMonths: number | null
  ) {
    const partner = this.rules().find(
      (r) => r.beneficiaryStaffId === staffId && r.ruleKind === PARTNER_SHARE
    );
    const bonus = this.rules().find(
      (r) => r.beneficiaryStaffId === staffId && r.ruleKind === QUARTERLY_BONUS
    );
    const updates = [];
    if (partner && partnerPercent != null) {
      updates.push(this.profitApi.updateRule(partner.id, { ratePercent: partnerPercent }));
    }
    if (bonus && bonusMonths != null) {
      updates.push(
        this.profitApi.updateRule(bonus.id, { salaryMonthsEquivalent: bonusMonths, enabled: true })
      );
    }
    return updates.length ? forkJoin(updates) : of([]);
  }
}
