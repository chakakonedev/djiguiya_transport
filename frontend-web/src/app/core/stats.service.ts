import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from './api.config';
import { ApiResponse, CompareMonthRow, StatsSummary, YoyRow } from './api.types';

export interface StatsFilterParams {
  vehicleId?: string;
  driverId?: string;
  month?: number;
  year?: number;
  category?: string;
}

@Injectable({ providedIn: 'root' })
export class StatsService {
  private readonly base = `${API_BASE_URL}/stats`;

  constructor(private readonly http: HttpClient) {}

  summary(filters: StatsFilterParams = {}) {
    return this.http
      .get<ApiResponse<StatsSummary>>(`${this.base}/summary`, { params: this.toParams(filters) })
      .pipe(map((res) => res.data));
  }

  compareMonth(monthIndex: number, filters: Omit<StatsFilterParams, 'month'> = {}) {
    let params = this.toParams(filters);
    params = params.set('monthIndex', String(monthIndex));
    return this.http
      .get<ApiResponse<CompareMonthRow>>(`${this.base}/compare-month`, { params })
      .pipe(map((res) => res.data));
  }

  yoy(year: number, filters: Omit<StatsFilterParams, 'year'> = {}) {
    let params = this.toParams(filters);
    params = params.set('year', String(year));
    return this.http
      .get<ApiResponse<YoyRow[]>>(`${this.base}/yoy`, { params })
      .pipe(map((res) => res.data ?? []));
  }

  private toParams(filters: StatsFilterParams): HttpParams {
    let params = new HttpParams();
    if (filters.vehicleId) params = params.set('vehicleId', filters.vehicleId);
    if (filters.driverId) params = params.set('driverId', filters.driverId);
    if (filters.month != null) params = params.set('month', String(filters.month));
    if (filters.year != null) params = params.set('year', String(filters.year));
    if (filters.category) params = params.set('category', filters.category);
    return params;
  }
}
