import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from './api.config';
import { ApiResponse, LedgerTransaction, TransactionCreateRequest } from './api.types';

@Injectable({ providedIn: 'root' })
export class TransactionService {
  private readonly base = `${API_BASE_URL}/transactions`;

  constructor(private readonly http: HttpClient) {}

  list(filters?: { type?: string; monthKey?: string }) {
    let params = new HttpParams();
    if (filters?.type) params = params.set('type', filters.type);
    if (filters?.monthKey) params = params.set('monthKey', filters.monthKey);
    return this.http
      .get<ApiResponse<LedgerTransaction[]>>(this.base, { params })
      .pipe(map((res) => res.data ?? []));
  }

  create(body: TransactionCreateRequest) {
    return this.http
      .post<ApiResponse<LedgerTransaction>>(this.base, body)
      .pipe(map((res) => res.data));
  }

  update(id: string, body: Partial<TransactionCreateRequest>) {
    return this.http
      .put<ApiResponse<LedgerTransaction>>(`${this.base}/${id}`, body)
      .pipe(map((res) => res.data));
  }

  remove(id: string) {
    return this.http.delete<ApiResponse<void>>(`${this.base}/${id}`).pipe(map(() => undefined));
  }
}
