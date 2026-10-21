import { Component, inject, OnInit, signal } from '@angular/core';
import { TranslatePipe } from '@ngx-translate/core';
import { TripService } from '../../core/trip.service';
import { VehicleService } from '../../core/vehicle.service';
import { Trip, Vehicle } from '../../core/api.types';

@Component({
  selector: 'app-trips-page',
  standalone: true,
  imports: [TranslatePipe],
  templateUrl: './trips.page.html',
  styleUrl: './trips.page.scss'
})
export class TripsPage implements OnInit {
  private readonly tripsApi = inject(TripService);
  private readonly vehiclesApi = inject(VehicleService);

  readonly trips = signal<Trip[]>([]);
  readonly vehicles = signal<Vehicle[]>([]);
  readonly loading = signal(true);
  readonly error = signal(false);

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

  vehicleLabel(vehicleId: string): string {
    return this.vehicles().find((v) => v.id === vehicleId)?.name ?? vehicleId;
  }
}
