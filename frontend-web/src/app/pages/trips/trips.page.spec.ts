import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { TripsPage } from './trips.page';
import { TripService } from '../../core/trip.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';

describe('TripsPage', () => {
  const tripsApi = { list: vi.fn() };
  const vehiclesApi = { list: vi.fn(() => of([{ id: 'v1', name: 'Bus 1' }])) };
  const staffApi = { list: vi.fn(() => of([{ id: 'd1', fullName: 'Amadou', staffType: 'driver' }])) };

  beforeEach(async () => {
    tripsApi.list.mockReset();
    await TestBed.configureTestingModule({
      imports: [TripsPage],
      providers: [
        provideTranslateService(),
        { provide: TripService, useValue: tripsApi },
        { provide: VehicleService, useValue: vehiclesApi },
        { provide: StaffService, useValue: staffApi }
      ]
    }).compileComponents();
  });

  it('loads trips and resolves labels', async () => {
    tripsApi.list.mockReturnValue(
      of([{ id: 't1', vehicleId: 'v1', driverId: 'd1', departureDate: '2026-03-01', returnDate: '2026-03-02' }])
    );
    const fixture = TestBed.createComponent(TripsPage);
    fixture.detectChanges();
    await fixture.whenStable();

    const page = fixture.componentInstance;
    expect(page.trips().length).toBe(1);
    expect(page.vehicleLabel('v1')).toBe('Bus 1');
    expect(page.driverLabel(page.trips()[0])).toBe('Amadou');
    expect(page.loading()).toBe(false);
  });

  it('sets error on failure', async () => {
    tripsApi.list.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(TripsPage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toBe(true);
  });

  it('driverLabel prefers trip.driverName and falls back', async () => {
    tripsApi.list.mockReturnValue(of([]));
    const fixture = TestBed.createComponent(TripsPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;
    expect(
      page.driverLabel({
        id: 't',
        vehicleId: 'v1',
        driverId: 'd1',
        driverName: 'Named',
        departureDate: '2026-01-01',
        returnDate: '2026-01-02',
        monthKey: '2026-01'
      })
    ).toBe('Named');
    expect(
      page.driverLabel({
        id: 't',
        vehicleId: 'v1',
        driverId: '',
        departureDate: '2026-01-01',
        returnDate: '2026-01-02',
        monthKey: '2026-01'
      } as never)
    ).toBe('—');
    expect(page.vehicleLabel('missing')).toBe('missing');
  });
});
