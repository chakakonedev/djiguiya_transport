import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { API_BASE_URL } from './api.config';
import {
  ApiResponse,
  PrimaryDriverRequest,
  Vehicle,
  VehicleCreateRequest,
  VehicleDocument
} from './api.types';

@Injectable({ providedIn: 'root' })
export class VehicleService {
  private readonly base = `${API_BASE_URL}/vehicles`;

  constructor(private readonly http: HttpClient) {}

  list() {
    return this.http
      .get<ApiResponse<Vehicle[]>>(this.base)
      .pipe(map((res) => res.data ?? []));
  }

  create(body: VehicleCreateRequest) {
    return this.http
      .post<ApiResponse<Vehicle>>(this.base, body)
      .pipe(map((res) => res.data));
  }

  update(id: string, body: Partial<VehicleCreateRequest>) {
    return this.http
      .put<ApiResponse<Vehicle>>(`${this.base}/${id}`, body)
      .pipe(map((res) => res.data));
  }

  setPrimaryDriver(vehicleId: string, body: PrimaryDriverRequest) {
    return this.http
      .post<ApiResponse<Vehicle>>(`${this.base}/${vehicleId}/primary-driver`, body)
      .pipe(map((res) => res.data));
  }

  getDocuments(vehicleId: string) {
    return this.http
      .get<ApiResponse<VehicleDocument[]>>(`${this.base}/${vehicleId}/documents`)
      .pipe(map((res) => res.data ?? []));
  }

  saveDocuments(vehicleId: string, documents: VehicleDocument[]) {
    return this.http
      .put<ApiResponse<VehicleDocument[]>>(`${this.base}/${vehicleId}/documents`, documents)
      .pipe(map((res) => res.data ?? []));
  }
}
