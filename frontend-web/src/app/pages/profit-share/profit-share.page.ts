import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { ProfitShareService } from '../../core/profit-share.service';
import { ProfitSharePreview } from '../../core/api.types';
import { fiscalStartYearContaining } from '../../core/fiscal-year.util';
import { formatXof } from '../../core/format.util';

/**
 * Annual profit distribution preview for a fiscal start year.
 * State: `preview`, `loading`, `error`; form: `year` (defaults to current fiscal year).
 */
@Component({
  selector: 'app-profit-share-page',
  standalone: true,
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './profit-share.page.html',
  styleUrl: './profit-share.page.scss'
})
export class ProfitSharePage implements OnInit {
  private readonly profitApi = inject(ProfitShareService);
  private readonly fb = inject(FormBuilder);

  readonly preview = signal<ProfitSharePreview | null>(null);
  readonly loading = signal(false);
  readonly error = signal(false);
  readonly formatXof = formatXof;

  readonly form = this.fb.nonNullable.group({
    year: [fiscalStartYearContaining(), [Validators.required, Validators.min(2000)]]
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    if (this.form.invalid) return;
    this.loading.set(true);
    this.error.set(false);
    const year = this.form.controls.year.value;
    this.profitApi.preview(year).subscribe({
      next: (data) => {
        this.preview.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }
}
