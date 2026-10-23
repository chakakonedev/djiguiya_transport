import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { ShellLayout } from './shell.layout';
import { AuthService } from '../../core/auth.service';

/**
 * App chrome (nav + auth): ensures main routes are linked and AuthService
 * is available for the header (user display / logout).
 */
describe('ShellLayout', () => {
  it('exposes nav links and auth', async () => {
    const auth = { user: () => ({ fullName: 'Owner' }), logout: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [ShellLayout],
      providers: [
        provideRouter([]),
        provideTranslateService(),
        { provide: AuthService, useValue: auth }
      ]
    }).compileComponents();

    const fixture = TestBed.createComponent(ShellLayout);
    fixture.detectChanges();
    expect(fixture.componentInstance.navLinks.length).toBeGreaterThan(3);
    expect(fixture.componentInstance.navLinks.some((l) => l.path === '/ledger')).toBe(true);
    expect(fixture.componentInstance.auth).toBe(auth);
  });
});
