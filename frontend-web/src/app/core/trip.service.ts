import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from './api.config';
import { ApiResponse, Trip, TripCreateRequest } from './api.types';

@Injectable({ providedIn: 'root' })
export class TripService {
  private readonly base = `${API_BASE_URL}/trips`;

  constructor(private readonly http: HttpClient) {}

  list(filters?: { vehicleId?: string; monthKey?: string }) {
    let params = new HttpParams();
    if (filters?.vehicleId) params = params.set('vehicleId', filters.vehicleId);
    if (filters?.monthKey) params = params.set('monthKey', filters.monthKey);
    return this.http
      .get<ApiResponse<Trip[]>>(this.base, { params })
      .pipe(map((res) => res.data ?? []));
  }

  create(body: TripCreateRequest) {
    return this.http
      .post<ApiResponse<Trip>>(this.base, body)
      .pipe(map((res) => res.data));
  }

  update(id: string, body: Partial<TripCreateRequest>) {
    return this.http
      .put<ApiResponse<Trip>>(`${this.base}/${id}`, body)
      .pipe(map((res) => res.data));
  }
}
