import { TestBed } from '@angular/core/testing';
import { provideTranslateService } from '@ngx-translate/core';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LedgerPage } from './ledger.page';
import { TransactionService } from '../../core/transaction.service';
import { TripService } from '../../core/trip.service';
import { VehicleService } from '../../core/vehicle.service';
import { StaffService } from '../../core/staff.service';
import { AuthService } from '../../core/auth.service';

/**
 * LedgerPage component tests with mocked services: data loading, tab/forms,
 * expense and revenue submission, edit/delete, styling helpers, and list errors.
 */
describe('LedgerPage component', () => {
  const txnApi = {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    remove: vi.fn()
  };
  const tripsApi = { get: vi.fn(), create: vi.fn(), update: vi.fn() };
  const vehiclesApi = {
    list: vi.fn(() =>
      of([
        { id: 'v1', name: 'Bus 1', status: 'active', primaryDriverId: 'd1' },
        { id: 'v2', name: 'Bus 2', status: 'inactive' }
      ])
    )
  };
  const staffApi = {
    list: vi.fn(() =>
      of([
        { id: 'd1', fullName: 'Amadou', staffType: 'driver', status: 'active' },
        { id: 'x1', fullName: 'Cashier', staffType: 'cashier', status: 'active' }
      ])
    )
  };
  const auth = { user: vi.fn(() => ({ roleCode: 'owner' })) };

  beforeEach(async () => {
    Object.values(txnApi).forEach((fn) => fn.mockReset());
    tripsApi.get.mockReset();
    tripsApi.create.mockReset();
    tripsApi.update.mockReset();
    txnApi.list.mockReturnValue(of([]));

    await TestBed.configureTestingModule({
      imports: [LedgerPage],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: TransactionService, useValue: txnApi },
        { provide: TripService, useValue: tripsApi },
        { provide: VehicleService, useValue: vehiclesApi },
        { provide: StaffService, useValue: staffApi },
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();
  });

  /** Init filters to active fleet/staff, loads transactions, and grants write for owner. */
  it('loads active vehicles/drivers and transactions', async () => {
    txnApi.list.mockReturnValue(
      of([{ id: 'tx1', type: 'expense', amount: 10, txnDate: '2026-03-01', label: 'Fuel', categoryCode: 'bus' }])
    );
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();

    const page = fixture.componentInstance;
    expect(page.vehicles().map((v) => v.id)).toEqual(['v1']);
    expect(page.drivers().map((d) => d.id)).toEqual(['d1']);
    expect(page.transactions().length).toBe(1);
    expect(page.canWrite()).toBe(true);
  });

  /** CSS tone helpers reflect revenue vs expense and fuel subcategory. */
  it('type and amount tone classes', async () => {
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;

    expect(page.typeToneClass({ id: '1', type: 'revenue', amount: 1, txnDate: '2026-01-01', label: 'x', monthKey: '2026-01' })).toContain(
      'revenue'
    );
    expect(
      page.typeToneClass({
        id: '2',
        type: 'expense',
        amount: 1,
        txnDate: '2026-01-01',
        label: 'x',
        monthKey: '2026-01',
        subcategoryCode: 'fuel'
      })
    ).toContain('fuel');
    expect(
      page.amountToneClass({
        id: '3',
        type: 'expense',
        amount: 1,
        txnDate: '2026-01-01',
        label: 'x',
        monthKey: '2026-01'
      })
    ).toContain('expense');
  });

  /** Expense tab toggles validators for "other" subgroup and resets subcategory on category change. */
  it('switches tab and applies other subgroup validators', async () => {
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;

    page.setTab('expense');
    expect(page.tab()).toBe('expense');
    page.expenseForm.controls.subcategoryCode.setValue('other');
    expect(page.otherSubgroupSelected()).toBe(true);
    expect(page.expenseForm.controls.label.hasError('required')).toBe(true);

    page.onCategoryChange();
    expect(page.expenseForm.controls.subcategoryCode.value).toBe(page.expenseSubgroups()[0]);
  });

  /** Valid expense form posts create with default label when label is empty. */
  it('submits expense create', async () => {
    txnApi.create.mockReturnValue(of({ id: 'e1' }));
    txnApi.list.mockReturnValue(of([]));
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;

    page.setTab('expense');
    page.expenseForm.patchValue({
      txnDate: '2026-03-01',
      amount: 5000,
      categoryCode: 'bus',
      subcategoryCode: 'fuel',
      label: ''
    });
    page.submitExpense();

    expect(txnApi.create).toHaveBeenCalledWith(
      expect.objectContaining({ type: 'expense', amount: 5000, subcategoryCode: 'fuel', label: 'Dépense' })
    );
  });

  /** Revenue flow creates a trip first, then links revenue transaction to tripId. */
  it('submits revenue create via trip', async () => {
    tripsApi.create.mockReturnValue(
      of({ id: 't1', vehicleId: 'v1', driverId: 'd1', departureDate: '2026-03-01', returnDate: '2026-03-02' })
    );
    txnApi.create.mockReturnValue(of({ id: 'r1' }));
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;

    page.revenueForm.patchValue({
      vehicleId: 'v1',
      driverId: 'd1',
      departureDate: '2026-03-01',
      returnDate: '2026-03-02',
      txnDate: '2026-03-02',
      amount: 100000,
      label: ''
    });
    page.submitRevenue();

    expect(tripsApi.create).toHaveBeenCalled();
    expect(txnApi.create).toHaveBeenCalledWith(
      expect.objectContaining({ type: 'revenue', tripId: 't1', label: 'Versement voyage' })
    );
  });

  /** startEdit hydrates expense form; submitExpense calls update when editingId is set. */
  it('starts expense edit and updates', async () => {
    txnApi.update.mockReturnValue(of({ id: 'e1' }));
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;

    page.startEdit({
      id: 'e1',
      type: 'expense',
      amount: 10,
      txnDate: '2026-03-01',
      label: 'Dépense',
      monthKey: '2026-03',
      categoryCode: 'family',
      subcategoryCode: 'electricity',
      notes: null
    });
    expect(page.editingId()).toBe('e1');
    expect(page.tab()).toBe('expense');
    page.expenseForm.patchValue({ amount: 20 });
    page.submitExpense();
    expect(txnApi.update).toHaveBeenCalled();
  });

  /** Revenue edit loads trip details and preserves custom label on revenue form. */
  it('starts revenue edit from trip', async () => {
    tripsApi.get.mockReturnValue(
      of({
        id: 't1',
        vehicleId: 'v1',
        driverId: 'd1',
        departureDate: '2026-03-01',
        returnDate: '2026-03-02',
        monthKey: '2026-03'
      })
    );
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;

    page.startEdit({
      id: 'r1',
      type: 'revenue',
      amount: 100,
      txnDate: '2026-03-02',
      label: 'Custom',
      monthKey: '2026-03',
      tripId: 't1',
      vehicleId: 'v1',
      driverId: 'd1'
    });
    await fixture.whenStable();
    expect(page.editingTripId()).toBe('t1');
    expect(page.revenueForm.controls.label.value).toBe('Custom');
  });

  /** Deletion proceeds only after confirm; calls TransactionService.remove. */
  it('remove asks confirm and deletes', async () => {
    const confirmSpy = vi.spyOn(window, 'confirm').mockReturnValue(true);
    txnApi.remove.mockReturnValue(of(undefined));
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();
    const page = fixture.componentInstance;

    page.remove({
      id: 'e1',
      type: 'expense',
      amount: 1,
      txnDate: '2026-03-01',
      label: 'x',
      monthKey: '2026-03'
    });
    expect(txnApi.remove).toHaveBeenCalledWith('e1');
    confirmSpy.mockRestore();
  });

  /** Failed transaction list sets the page error signal. */
  it('sets error when list fails', async () => {
    txnApi.list.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(LedgerPage);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.componentInstance.error()).toBe(true);
  });
});
