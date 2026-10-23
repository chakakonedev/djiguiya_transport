import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from './api.config';
import { ApiResponse, StaffCreateRequest, StaffMember } from './api.types';

/**
 * Staff directory (drivers, cashiers, etc.).
 * Base path: GET|POST /staff, PUT /staff/:id.
 */
@Injectable({ providedIn: 'root' })
export class StaffService {
  private readonly base = `${API_BASE_URL}/staff`;

  constructor(private readonly http: HttpClient) {}

  list() {
    return this.http
      .get<ApiResponse<StaffMember[]>>(this.base)
      .pipe(map((res) => res.data ?? []));
  }

  create(body: StaffCreateRequest) {
    return this.http
      .post<ApiResponse<StaffMember>>(this.base, body)
      .pipe(map((res) => res.data));
  }

  update(id: string, body: Partial<StaffCreateRequest>) {
    return this.http
      .put<ApiResponse<StaffMember>>(`${this.base}/${id}`, body)
      .pipe(map((res) => res.data));
  }
}
