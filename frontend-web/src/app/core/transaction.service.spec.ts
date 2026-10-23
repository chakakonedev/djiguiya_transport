import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TransactionService } from './transaction.service';
import { API_BASE_URL } from './api.config';

/**
 * TransactionService HTTP contract: filtered list, create, update, and DELETE remove.
 */
describe('TransactionService', () => {
  let service: TransactionService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(TransactionService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  /** CRUD operations target /transactions with query params and unwrap responses. */
  it('list create update remove', () => {
    service.list({ type: 'expense', monthKey: '2026-03' }).subscribe();
    const list = http.expectOne(
      (r) => r.url === `${API_BASE_URL}/transactions` && r.params.get('type') === 'expense'
    );
    list.flush({ success: true, messageKey: null, data: [] });

    service
      .create({
        type: 'expense',
        txnDate: '2026-03-01',
        amount: 10,
        categoryCode: 'bus',
        subcategoryCode: 'fuel'
      })
      .subscribe();
    http.expectOne(`${API_BASE_URL}/transactions`).flush({
      success: true,
      messageKey: null,
      data: { id: 'tx1' }
    });

    service.update('tx1', { amount: 20 }).subscribe();
    http.expectOne(`${API_BASE_URL}/transactions/tx1`).flush({
      success: true,
      messageKey: null,
      data: { id: 'tx1', amount: 20 }
    });

    service.remove('tx1').subscribe();
    http.expectOne(`${API_BASE_URL}/transactions/tx1`).flush({ success: true, messageKey: null, data: null });
  });
});
