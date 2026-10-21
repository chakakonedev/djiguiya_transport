import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TranslatePipe } from '@ngx-translate/core';
import { TripService } from '../../core/trip.service';
import { VehicleService } from '../../core/vehicle.service';
import { Trip, Vehicle } from '../../core/api.types';

@Component({
  selector: 'app-trips-page',
  standalone: true,
  imports: [ReactiveFormsModule, TranslatePipe],
  templateUrl: './trips.page.html',
  styleUrl: './trips.page.scss'
})
export class TripsPage implements OnInit {
  private readonly tripsApi = inject(TripService);
  private readonly vehiclesApi = inject(VehicleService);
  private readonly fb = inject(FormBuilder);

  readonly trips = signal<Trip[]>([]);
  readonly vehicles = signal<Vehicle[]>([]);
  readonly loading = signal(true);
  readonly saving = signal(false);
  readonly error = signal(false);

  readonly form = this.fb.nonNullable.group({
    vehicleId: ['', Validators.required],
    departureDate: ['', Validators.required],
    returnDate: ['', Validators.required],
    notes: ['']
  });

  ngOnInit(): void {
    this.vehiclesApi.list().subscribe({
      next: (list) => this.vehicles.set(list)
    });
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.tripsApi.list().subscribe({
      next: (list) => {
        this.trips.set(list);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(true);
        this.loading.set(false);
      }
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    this.saving.set(true);
    const raw = this.form.getRawValue();
    this.tripsApi
      .create({
        vehicleId: raw.vehicleId,
        departureDate: raw.departureDate,
        returnDate: raw.returnDate,
        notes: raw.notes || null
      })
      .subscribe({
        next: () => {
          this.form.reset({
            vehicleId: '',
            departureDate: '',
            returnDate: '',
            notes: ''
          });
          this.saving.set(false);
          this.reload();
        },
        error: () => {
          this.saving.set(false);
          this.error.set(true);
        }
      });
  }

  vehicleLabel(vehicleId: string): string {
    return this.vehicles().find((v) => v.id === vehicleId)?.name ?? vehicleId;
  }
}
