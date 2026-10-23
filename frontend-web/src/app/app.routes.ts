import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

/**
 * Application routes: public login, lazy-loaded shell for authenticated area,
 * and feature pages loaded on demand to keep initial bundle small.
 */
export const routes: Routes = [
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.page').then((m) => m.LoginPage)
  },
  {
    path: '',
    canActivate: [authGuard],
    loadComponent: () => import('./layout/shell/shell.layout').then((m) => m.ShellLayout),
    children: [
      {
        path: '',
        loadComponent: () => import('./pages/home/home.page').then((m) => m.HomePage)
      },
      {
        path: 'fleet',
        loadComponent: () => import('./pages/fleet/fleet.page').then((m) => m.FleetPage)
      },
      {
        path: 'staff',
        loadComponent: () => import('./pages/staff/staff.page').then((m) => m.StaffPage)
      },
      {
        path: 'trips',
        loadComponent: () => import('./pages/trips/trips.page').then((m) => m.TripsPage)
      },
      {
        path: 'ledger',
        loadComponent: () => import('./pages/ledger/ledger.page').then((m) => m.LedgerPage)
      },
      {
        path: 'stats',
        loadComponent: () => import('./pages/stats/stats.page').then((m) => m.StatsPage)
      },
      {
        path: 'profit-share',
        loadComponent: () =>
          import('./pages/profit-share/profit-share.page').then((m) => m.ProfitSharePage)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
