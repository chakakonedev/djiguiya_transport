import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TripService } from './trip.service';
import { API_BASE_URL } from './api.config';

describe('TripService', () => {
  let service: TripService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TripService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('list get create update', () => {
    service.list({ vehicleId: 'v1', monthKey: '2026-03' }).subscribe();
    const list = http.expectOne(
      (r) => r.url === `${API_BASE_URL}/trips` && r.params.get('vehicleId') === 'v1'
    );
    list.flush({ success: true, messageKey: null, data: [] });

    service.get('t1').subscribe();
    http.expectOne(`${API_BASE_URL}/trips/t1`).flush({
      success: true,
      messageKey: null,
      data: { id: 't1', vehicleId: 'v1' }
    });

    service
      .create({
        vehicleId: 'v1',
        departureDate: '2026-03-01',
        returnDate: '2026-03-02',
        driverId: 'd1'
      })
      .subscribe();
    http.expectOne(`${API_BASE_URL}/trips`).flush({ success: true, messageKey: null, data: { id: 't1' } });

    service.update('t1', { notes: 'x' }).subscribe();
    http.expectOne(`${API_BASE_URL}/trips/t1`).flush({ success: true, messageKey: null, data: { id: 't1' } });
  });
});
