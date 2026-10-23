import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { TranslatePipe } from '@ngx-translate/core';
import { AuthService } from '../../core/auth.service';

/**
 * Authenticated app chrome: top bar, primary navigation, and nested feature routes.
 */
@Component({
  selector: 'app-shell-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, TranslatePipe],
  templateUrl: './shell.layout.html',
  styleUrl: './shell.layout.scss'
})
export class ShellLayout {
  readonly navLinks = [
    { path: '/', labelKey: 'nav.home', exact: true },
    { path: '/trips', labelKey: 'nav.trips', exact: false },
    { path: '/ledger', labelKey: 'nav.ledger', exact: false },
    { path: '/fleet', labelKey: 'nav.fleet', exact: false },
    { path: '/staff', labelKey: 'nav.staff', exact: false },
    { path: '/stats', labelKey: 'nav.stats', exact: false },
    { path: '/profit-share', labelKey: 'nav.profitShare', exact: false }
  ];

  constructor(readonly auth: AuthService) {}
}
