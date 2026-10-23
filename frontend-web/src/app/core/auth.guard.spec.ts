import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

/**
 * authGuard: allows navigation when logged in; otherwise redirects to /login.
 */
describe('authGuard', () => {
  /** Authenticated session returns true without building a UrlTree. */
  it('allows logged-in users', () => {
    const auth = { isLoggedIn: () => true };
    const router = { createUrlTree: vi.fn() };
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router }
      ]
    });
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toBe(true);
    expect(router.createUrlTree).not.toHaveBeenCalled();
  });

  /** Anonymous users get a UrlTree for /login from Router.createUrlTree. */
  it('redirects to login when anonymous', () => {
    const tree = {} as UrlTree;
    const auth = { isLoggedIn: () => false };
    const router = { createUrlTree: vi.fn(() => tree) };
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: auth },
        { provide: Router, useValue: router }
      ]
    });
    const result = TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
    expect(result).toBe(tree);
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login']);
  });
});
