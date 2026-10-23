import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { LoginPage } from './login.page';
import { AuthService } from '../../core/auth.service';

/**
 * LoginPage: form defaults, validation gate before submit, and error/loading on auth result.
 */
describe('LoginPage', () => {
  const authMock = {
    login: vi.fn()
  };

  beforeEach(async () => {
    authMock.login.mockReset();
    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [provideRouter([]), provideTranslateService(), { provide: AuthService, useValue: authMock }]
    }).compileComponents();
  });

  /** Component bootstraps under TestBed with mocked AuthService. */
  it('creates the component', () => {
    const fixture = TestBed.createComponent(LoginPage);
    expect(fixture.componentInstance).toBeTruthy();
  });

  /** Email and password start empty — no demo credentials in the form. */
  it('does not prefill credentials', () => {
    const fixture = TestBed.createComponent(LoginPage);
    const page = fixture.componentInstance;
    expect(page.form.getRawValue()).toEqual({ email: '', password: '' });
  });

  /** submit() is a no-op when required fields fail validation. */
  it('does not submit when form is empty', () => {
    const fixture = TestBed.createComponent(LoginPage);
    const page = fixture.componentInstance;
    page.submit();
    expect(authMock.login).not.toHaveBeenCalled();
  });

  /** Failed login observable sets error and clears loading. */
  it('sets error on failed login', () => {
    authMock.login.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(LoginPage);
    const page = fixture.componentInstance;
    page.form.setValue({ email: 'a@b.ml', password: 'password' });
    page.submit();
    expect(page.error()).toBe(true);
    expect(page.loading()).toBe(false);
  });

  /** Successful login invokes AuthService.login and resets loading (error cleared on success path). */
  it('clears error on successful login', () => {
    authMock.login.mockReturnValue(of({ success: true, data: {} }));
    const fixture = TestBed.createComponent(LoginPage);
    const page = fixture.componentInstance;
    page.error.set(true);
    page.form.setValue({ email: 'a@b.ml', password: 'password' });
    page.submit();
    expect(authMock.login).toHaveBeenCalled();
    expect(page.loading()).toBe(false);
  });
});
