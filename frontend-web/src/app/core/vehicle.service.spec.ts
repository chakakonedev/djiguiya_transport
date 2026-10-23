import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { VehicleService } from './vehicle.service';
import { API_BASE_URL } from './api.config';

/**
 * VehicleService HTTP contract: unwraps API envelopes and hits the expected
 * REST paths for fleet CRUD, primary driver assignment, and documents.
 */
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

  /** GET /vehicles → returns the inner `data` array, not the wrapper. */
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

  /** POST /vehicles with create payload. */
  it('creates a vehicle', () => {
    service.create({ name: 'Bus 2', plate: null, purchasePrice: 1, purchaseDate: null }).subscribe();

    const req = http.expectOne(`${API_BASE_URL}/vehicles`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body.name).toBe('Bus 2');
    req.flush({ success: true, messageKey: null, data: { id: 'v2', name: 'Bus 2' } });
  });

  /** POST /vehicles/:id/primary-driver includes startDate for assignment history. */
  it('assigns primary driver with startDate', () => {
    service.setPrimaryDriver('v1', { driverId: 'd1', startDate: '2026-10-21' }).subscribe();

    const req = http.expectOne(`${API_BASE_URL}/vehicles/v1/primary-driver`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ driverId: 'd1', startDate: '2026-10-21' });
    req.flush({
      success: true,
      messageKey: 'vehicle.primary_driver_set',
      data: { id: 'v1', name: 'bus1', status: 'active', primaryDriverId: 'd1' }
    });
  });

  /** PUT /vehicles/:id/documents sends the full document array as body. */
  it('saves documents as an array', () => {
    const docs = [
      {
        docType: 'insurance',
        expiresOn: '2027-01-01',
        lastRenewedOn: '2026-01-01',
        renewalPeriodMonths: 12,
        lastAmount: 100000
      }
    ];
    service.saveDocuments('v1', docs).subscribe();

    const req = http.expectOne(`${API_BASE_URL}/vehicles/v1/documents`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(docs);
    req.flush({ success: true, messageKey: 'documents.updated', data: docs });
  });

  /** PATCH update + GET documents smoke coverage for remaining endpoints. */
  it('updates vehicle and loads documents', () => {
    service.update('v1', { name: 'Renamed' }).subscribe();
    http.expectOne(`${API_BASE_URL}/vehicles/v1`).flush({
      success: true,
      messageKey: null,
      data: { id: 'v1', name: 'Renamed', status: 'active' }
    });

    service.getDocuments('v1').subscribe();
    http.expectOne(`${API_BASE_URL}/vehicles/v1/documents`).flush({
      success: true,
      messageKey: null,
      data: []
    });
  });
});
