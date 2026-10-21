import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { FleetPage } from './fleet.page';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';

describe('FleetPage', () => {
  const vehiclesApi = {
    list: vi.fn(),
    create: vi.fn(),
    setPrimaryDriver: vi.fn(),
    getDocuments: vi.fn(),
    saveDocuments: vi.fn()
  };
  const staffApi = {
    list: vi.fn()
  };

  beforeEach(async () => {
    Object.values(vehiclesApi).forEach((fn) => fn.mockReset());
    staffApi.list.mockReset();
    vehiclesApi.list.mockReturnValue(of([{ id: 'v1', name: 'bus1', status: 'active' }]));
    vehiclesApi.getDocuments.mockReturnValue(of([]));
    staffApi.list.mockReturnValue(
      of([{ id: 'd1', fullName: 'Tièdiè Koné', staffType: 'driver', status: 'active' }])
    );

    await TestBed.configureTestingModule({
      imports: [FleetPage],
      providers: [
        provideTranslateService(),
        { provide: VehicleService, useValue: vehiclesApi },
        { provide: StaffService, useValue: staffApi }
      ]
    }).compileComponents();
  });

  it('sends startDate when saving primary driver', () => {
    vehiclesApi.setPrimaryDriver.mockReturnValue(
      of({ id: 'v1', name: 'bus1', status: 'active', primaryDriverId: 'd1' })
    );
    const fixture = TestBed.createComponent(FleetPage);
    const page = fixture.componentInstance;
    fixture.detectChanges();

    page.toggleExpand({ id: 'v1', name: 'bus1', status: 'active' });
    page.driverForm('v1').setValue({ driverId: 'd1', startDate: '2026-10-21' });
    page.savePrimaryDriver('v1');

    expect(vehiclesApi.setPrimaryDriver).toHaveBeenCalledWith('v1', {
      driverId: 'd1',
      startDate: '2026-10-21'
    });
  });

  it('defaults startDate to today in the assign form', () => {
    const fixture = TestBed.createComponent(FleetPage);
    const page = fixture.componentInstance;
    fixture.detectChanges();

    page.toggleExpand({ id: 'v1', name: 'bus1', status: 'active' });
    const startDate = page.driverForm('v1').getRawValue().startDate as string;
    expect(startDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  it('keeps the vehicle list visible when assign fails', () => {
    vehiclesApi.setPrimaryDriver.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(FleetPage);
    const page = fixture.componentInstance;
    fixture.detectChanges();

    page.toggleExpand({ id: 'v1', name: 'bus1', status: 'active' });
    page.driverForm('v1').setValue({ driverId: 'd1', startDate: '2026-10-21' });
    page.savePrimaryDriver('v1');

    expect(page.actionError()).toBe(true);
    expect(page.loadError()).toBe(false);
    expect(page.vehicles().length).toBe(1);
  });
});
