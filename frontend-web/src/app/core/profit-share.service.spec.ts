import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ProfitShareService } from './profit-share.service';
import { API_BASE_URL } from './api.config';

/**
 * ProfitShareService: preview by fiscal year, list rules, and PATCH individual rules.
 */
describe('ProfitShareService', () => {
  let service: ProfitShareService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProfitShareService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  /** preview, listRules, and updateRule call the correct profit-share endpoints. */
  it('preview listRules updateRule', () => {
    service.preview(2025).subscribe();
    http.expectOne(`${API_BASE_URL}/profit-share/preview?year=2025`).flush({
      success: true,
      messageKey: null,
      data: { year: 2025, lines: [] }
    });

    service.listRules().subscribe();
    http.expectOne(`${API_BASE_URL}/profit-share/rules`).flush({
      success: true,
      messageKey: null,
      data: []
    });

    service.updateRule('r1', { ratePercent: 2.5, enabled: true }).subscribe();
    http.expectOne(`${API_BASE_URL}/profit-share/rules/r1`).flush({
      success: true,
      messageKey: null,
      data: { id: 'r1', ratePercent: 2.5 }
    });
  });
});
