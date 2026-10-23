import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { FleetPage } from './fleet.page';
import { AuthService } from '../../core/auth.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';

/**
 * FleetPage: vehicle list, primary-driver assignment (with startDate),
 * document forms, and create flow — action errors must not wipe the list.
 */
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
  const auth = { user: vi.fn(() => ({ roleCode: 'owner' })) };

  beforeEach(async () => {
    Object.values(vehiclesApi).forEach((fn) => fn.mockReset());
    staffApi.list.mockReset();
    auth.user.mockReset();
    auth.user.mockReturnValue({ roleCode: 'owner' });
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
        { provide: StaffService, useValue: staffApi },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();
  });

  /** Assign primary driver forwards startDate to the API. */
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

  /** Expanding a row pre-fills startDate with today's ISO date. */
  it('defaults startDate to today in the assign form', () => {
    const fixture = TestBed.createComponent(FleetPage);
    const page = fixture.componentInstance;
    fixture.detectChanges();

    page.toggleExpand({ id: 'v1', name: 'bus1', status: 'active' });
    const startDate = page.driverForm('v1').getRawValue().startDate as string;
    expect(startDate).toMatch(/^\d{4}-\d{2}-\d{2}$/);
  });

  /** Assign failure is an actionError; list stays loaded (not loadError). */
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

  /** Create then reloads the vehicle list. */
  it('creates a vehicle and reloads', () => {
    vehiclesApi.create.mockReturnValue(of({ id: 'v2', name: 'Bus 2', status: 'active' }));
    const fixture = TestBed.createComponent(FleetPage);
    const page = fixture.componentInstance;
    fixture.detectChanges();

    page.createForm.setValue({ name: 'Bus 2', plate: 'ML-2', purchasePrice: 1000, purchaseDate: '2026-01-01' });
    page.submitCreate();

    expect(vehiclesApi.create).toHaveBeenCalledWith({
      name: 'Bus 2',
      plate: 'ML-2',
      purchasePrice: 1000,
      purchaseDate: '2026-01-01'
    });
    expect(vehiclesApi.list.mock.calls.length).toBeGreaterThan(1);
  });

  /** Prefer primaryDriverName when present; else resolve from staff list. */
  it('resolves driver display name from staff list', () => {
    const fixture = TestBed.createComponent(FleetPage);
    const page = fixture.componentInstance;
    fixture.detectChanges();
    expect(page.driverDisplayName({ id: 'v1', name: 'bus1', status: 'active', primaryDriverId: 'd1' })).toBe(
      'Tièdiè Koné'
    );
    expect(page.driverDisplayName({ id: 'v1', name: 'bus1', status: 'active', primaryDriverName: 'Named' })).toBe(
      'Named'
    );
  });

  /** Document save only includes rows that have expiresOn set. */
  it('saves documents with expiresOn only', () => {
    vehiclesApi.saveDocuments.mockReturnValue(of([]));
    const fixture = TestBed.createComponent(FleetPage);
    const page = fixture.componentInstance;
    fixture.detectChanges();

    page.toggleExpand({ id: 'v1', name: 'bus1', status: 'active' });
    page.syncDocForm('v1', []);
    page.docForm('v1')!.get('insurance')!.patchValue({
      expiresOn: '2027-01-01',
      lastRenewedOn: '2026-01-01',
      renewalPeriodMonths: 12,
      lastAmount: 100000
    });
    page.saveDocuments('v1');

    expect(vehiclesApi.saveDocuments).toHaveBeenCalledWith(
      'v1',
      expect.arrayContaining([
        expect.objectContaining({ docType: 'insurance', expiresOn: '2027-01-01', lastAmount: 100000 })
      ])
    );
  });

  /** Initial list failure sets loadError. */
  it('sets loadError when list fails', () => {
    vehiclesApi.list.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(FleetPage);
    fixture.detectChanges();
    expect(fixture.componentInstance.loadError()).toBe(true);
  });
});
