import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from './api.config';
import { ApiResponse, ProfitSharePreview } from './api.types';

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
}
