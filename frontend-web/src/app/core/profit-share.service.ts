import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from './api.config';
import {
  ApiResponse,
  ProfitSharePreview,
  ProfitShareRule,
  ProfitShareRuleUpdateRequest
} from './api.types';

@Injectable({ providedIn: 'root' })
export class ProfitShareService {
  private readonly base = `${API_BASE_URL}/profit-share`;

  constructor(private readonly http: HttpClient) {}

  preview(year: number) {
    const params = new HttpParams().set('year', String(year));
    return this.http
      .get<ApiResponse<ProfitSharePreview>>(`${this.base}/preview`, { params })
      .pipe(map((res) => res.data));
  }

  listRules() {
    return this.http
      .get<ApiResponse<ProfitShareRule[]>>(`${this.base}/rules`)
      .pipe(map((res) => res.data ?? []));
  }

  updateRule(id: string, body: ProfitShareRuleUpdateRequest) {
    return this.http
      .put<ApiResponse<ProfitShareRule>>(`${this.base}/rules/${id}`, body)
      .pipe(map((res) => res.data));
  }
}
