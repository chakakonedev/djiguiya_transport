import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { API_BASE_URL } from './api.config';

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

  it('logout clears session and navigates to login', () => {
    localStorage.setItem('djiguiya_token', 'x');
    localStorage.setItem('djiguiya_user', JSON.stringify({ fullName: 'X' }));

    service.logout();

    expect(service.token()).toBeNull();
    expect(service.user()).toBeNull();
    expect(router.navigateByUrl).toHaveBeenCalledWith('/login');
  });
});
