import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { API_BASE_URL } from './api.config';

export interface LoginResponse {
  accessToken: string;
  userId: string;
  email: string;
  fullName: string;
  roleCode: string;
}

interface ApiResponse<T> {
  success: boolean;
  messageKey: string | null;
  data: T;
}

const TOKEN_KEY = 'djiguiya_token';
const USER_KEY = 'djiguiya_user';

/**
 * Session state: login against the API, persist JWT and user profile in localStorage,
 * and expose a reactive `user` signal for templates and role checks.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  readonly user = signal<LoginResponse | null>(this.readUser());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router
  ) {}

  /** POST /auth/login — stores token and user on success. */
  login(email: string, password: string) {
    return this.http
      .post<ApiResponse<LoginResponse>>(`${API_BASE_URL}/auth/login`, { email, password })
      .pipe(
        tap((res) => {
          localStorage.setItem(TOKEN_KEY, res.data.accessToken);
          localStorage.setItem(USER_KEY, JSON.stringify(res.data));
          this.user.set(res.data);
        })
      );
  }

  /** Clears persisted session and navigates to the login route. */
  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.user.set(null);
    void this.router.navigateByUrl('/login');
  }

  token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.token();
  }

  private readUser(): LoginResponse | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as LoginResponse;
    } catch {
      return null;
    }
  }
}
