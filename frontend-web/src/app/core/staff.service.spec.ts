import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { StaffService } from './staff.service';
import { API_BASE_URL } from './api.config';

/**
 * StaffService HTTP contract: list all staff, create, and PATCH update by id.
 */
describe('StaffService', () => {
  let service: StaffService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(StaffService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  /** list, create, and update hit /staff with expected methods and body shapes. */
  it('list create update', () => {
    service.list().subscribe();
    http.expectOne(`${API_BASE_URL}/staff`).flush({ success: true, messageKey: null, data: [] });

    service.create({ fullName: 'A', staffType: 'driver' }).subscribe();
    http.expectOne(`${API_BASE_URL}/staff`).flush({
      success: true,
      messageKey: null,
      data: { id: 's1', fullName: 'A', staffType: 'driver' }
    });

    service.update('s1', { fullName: 'B' }).subscribe();
    http.expectOne(`${API_BASE_URL}/staff/s1`).flush({
      success: true,
      messageKey: null,
      data: { id: 's1', fullName: 'B', staffType: 'driver' }
    });
  });
});
