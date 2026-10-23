import { Component, inject, OnInit, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/auth.service';
import { StatsService } from '../../core/stats.service';
import { StatsSummary } from '../../core/api.types';
import { formatXof } from '../../core/format.util';

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './home.page.html',
  styleUrl: './home.page.scss'
})
export class HomePage implements OnInit {
  private readonly stats = inject(StatsService);
  readonly auth = inject(AuthService);

  readonly loading = signal(true);
  readonly error = signal(false);
  readonly summary = signal<StatsSummary | null>(null);
  readonly formatXof = formatXof;

  ngOnInit(): void {
    // Full ledger overview (no calendar-year filter) so year-1 Aug→Jul is included.
    this.stats.summary({}).subscribe({
      next: (data) => {
        this.summary.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }
}
