import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { HomePage } from './home.page';
import { StatsService } from '../../core/stats.service';
import { AuthService } from '../../core/auth.service';

describe('HomePage', () => {
  const statsMock = { summary: vi.fn() };
  const authMock = { user: vi.fn(() => ({ fullName: 'Test User' })) };

  beforeEach(async () => {
    statsMock.summary.mockReset();
    await TestBed.configureTestingModule({
      imports: [HomePage],
      providers: [
        provideTranslateService(),
        { provide: StatsService, useValue: statsMock },
        { provide: AuthService, useValue: authMock }
      ]
    }).compileComponents();
  });

  it('loads summary on init', async () => {
    statsMock.summary.mockReturnValue(
      of({
        totalRevenue: 100,
        totalExpense: 40,
        netProfit: 60,
        tripCount: 2
      })
    );
    const fixture = TestBed.createComponent(HomePage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(statsMock.summary).toHaveBeenCalled();
    expect(fixture.componentInstance.summary()?.netProfit).toBe(60);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('shows error when summary fails', async () => {
    statsMock.summary.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(HomePage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toBe(true);
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
