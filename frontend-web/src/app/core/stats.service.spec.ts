import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { StatsService } from './stats.service';
import { API_BASE_URL } from './api.config';

/**
 * StatsService: summary with filter query params, monthly breakdown, and year-over-year.
 */
describe('StatsService', () => {
  let service: StatsService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(StatsService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  /** summary forwards vehicleId, month, year, and category as HTTP query params. */
  it('summary sends filters', () => {
    service.summary({ vehicleId: 'v1', month: 3, year: 2026, category: 'bus' }).subscribe();
    const req = http.expectOne(
      (r) => r.url === `${API_BASE_URL}/stats/summary` && r.params.get('vehicleId') === 'v1'
    );
    expect(req.request.params.get('month')).toBe('3');
    expect(req.request.params.get('category')).toBe('bus');
    req.flush({ success: true, messageKey: null, data: { totalRevenue: 1 } });
  });

  /** monthly and yoy build URLs with year and optional driverId filter. */
  it('monthly and yoy', () => {
    service.monthly(2025, { driverId: 'd1' }).subscribe();
    const monthly = http.expectOne(`${API_BASE_URL}/stats/monthly?driverId=d1&year=2025`);
    monthly.flush({ success: true, messageKey: null, data: { fiscalStartYear: 2025, months: [] } });

    service.yoy(2025).subscribe();
    const yoy = http.expectOne(`${API_BASE_URL}/stats/yoy?year=2025`);
    yoy.flush({ success: true, messageKey: null, data: { currentNetProfit: 0 } });
  });
});
