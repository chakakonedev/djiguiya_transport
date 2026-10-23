import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { ProfitSharePage } from './profit-share.page';
import { ProfitShareService } from '../../core/profit-share.service';

describe('ProfitSharePage', () => {
  const profitApi = { preview: vi.fn() };

  beforeEach(async () => {
    profitApi.preview.mockReset();
    await TestBed.configureTestingModule({
      imports: [ProfitSharePage],
      providers: [provideTranslateService(), { provide: ProfitShareService, useValue: profitApi }]
    }).compileComponents();
  });

  it('loads preview on init', async () => {
    profitApi.preview.mockReturnValue(
      of({
        year: 2025,
        periodStart: '2025-08-01',
        periodEnd: '2026-07-31',
        annualNetProfit: 100,
        reserveAmount: 10,
        distributableAmount: 90,
        lines: []
      })
    );
    const fixture = TestBed.createComponent(ProfitSharePage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(profitApi.preview).toHaveBeenCalled();
    expect(fixture.componentInstance.preview()?.annualNetProfit).toBe(100);
    expect(fixture.componentInstance.loading()).toBe(false);
  });

  it('sets error when preview fails', async () => {
    profitApi.preview.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(ProfitSharePage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toBe(true);
  });
});
