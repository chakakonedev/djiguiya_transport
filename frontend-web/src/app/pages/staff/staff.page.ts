import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { StaffService } from '../../core/staff.service';
import { StaffMember } from '../../core/api.types';
import { STAFF_TYPES } from '../../core/expense-catalog';
import { formatXof } from '../../core/format.util';

@Component({
  selector: 'app-staff-page',
  standalone: true,
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './staff.page.html',
  styleUrl: './staff.page.scss'
})
export class StaffPage implements OnInit {
  private readonly staffApi = inject(StaffService);
  private readonly fb = inject(FormBuilder);

  readonly staffList = signal<StaffMember[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal(false);
  readonly staffTypes = STAFF_TYPES;
  readonly formatXof = formatXof;

  readonly form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    staffType: ['driver', Validators.required],
    monthlySalary: [null as number | null],
    payDay: [null as number | null, [Validators.min(1), Validators.max(28)]]
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.staffApi.list().subscribe({
      next: (list) => {
        this.staffList.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const raw = this.form.getRawValue();
    this.staffApi
      .create({
        fullName: raw.fullName,
        staffType: raw.staffType,
        monthlySalary: raw.monthlySalary,
        payDay: raw.payDay
      })
      .subscribe({
        next: () => {
          this.form.reset({
            fullName: '',
            staffType: 'driver',
            monthlySalary: null,
            payDay: null
          });
          this.saving.set(false);
          this.reload();
        },
        error: () => {
          this.saving.set(false);
          this.error.set(true);
        }
      });
  }
}
