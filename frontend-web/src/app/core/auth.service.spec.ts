import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { API_BASE_URL } from './api.config';

/**
 * AuthService: login persistence (token + user in localStorage), logout navigation,
 * and resilience when stored user JSON is corrupt.
 */
describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;
  const router = { navigateByUrl: vi.fn() };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: router }
      ]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  /** Successful login hydrates signals from the API payload. */
  it('login stores token and user', () => {
    const payload = {
      accessToken: 'jwt-token',
      userId: 'u1',
      email: 'owner@djiguiya.ml',
      fullName: 'Owner',
      roleCode: 'owner'
    };

    service.login('owner@djiguiya.ml', 'password').subscribe((res) => {
      expect(res.data.accessToken).toBe('jwt-token');
    });

    const req = http.expectOne(`${API_BASE_URL}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush({ success: true, messageKey: null, data: payload });

    expect(service.token()).toBe('jwt-token');
    expect(service.user()?.fullName).toBe('Owner');
    expect(service.isLoggedIn()).toBe(true);
  });

  /** Logout clears session state and redirects to /login. */
  it('logout clears session and navigates to login', () => {
    localStorage.setItem('djiguiya_token', 'x');
    localStorage.setItem('djiguiya_user', JSON.stringify({ fullName: 'X' }));

    service.logout();

    expect(service.token()).toBeNull();
    expect(service.user()).toBeNull();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });

  /** Logged-in state is driven by token presence in storage. */
  it('isLoggedIn reflects token presence', () => {
    expect(service.isLoggedIn()).toBe(false);
    localStorage.setItem('djiguiya_token', 'jwt');
    expect(service.isLoggedIn()).toBe(true);
  });

  /** Corrupt JSON in storage must not crash bootstrap — user stays null. */
  it('ignores corrupt stored user JSON', () => {
    localStorage.setItem('djiguiya_user', '{not-json');
    // Rebuild the service so constructor re-reads localStorage
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: Router, useValue: router }
      ]
    });
    const rebuilt = TestBed.inject(AuthService);
    expect(rebuilt.user()).toBeNull();
  });
});
