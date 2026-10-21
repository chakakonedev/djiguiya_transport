import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { VehicleService } from './vehicle.service';
import { API_BASE_URL } from './api.config';

describe('VehicleService', () => {
  let service: VehicleService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(VehicleService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists vehicles from API wrapper', () => {
    let result: unknown;
    service.list().subscribe((data) => (result = data));

    const req = http.expectOne(`${API_BASE_URL}/vehicles`);
    expect(req.request.method).toBe('GET');
    req.flush({
      success: true,
      messageKey: null,
      data: [{ id: 'v1', name: 'Bus 1', plate: 'ML-01', status: 'active' }]
    });

    expect(result).toEqual([{ id: 'v1', name: 'Bus 1', plate: 'ML-01', status: 'active' }]);
  });

  it('creates a vehicle', () => {
    service.create({ name: 'Bus 2', plate: null, purchasePrice: 1, purchaseDate: null }).subscribe();

    const req = http.expectOne(`${API_BASE_URL}/vehicles`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.name).toBe('Bus 2');
    req.flush({ success: true, messageKey: null, data: { id: 'v2', name: 'Bus 2' } });
  });
});
