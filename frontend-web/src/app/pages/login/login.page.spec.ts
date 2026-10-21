import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { provideTranslateService } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';
import { LoginPage } from './login.page';
import { AuthService } from '../../core/auth.service';

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

  it('creates the component', () => {
    const fixture = TestBed.createComponent(LoginPage);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('does not prefill credentials', () => {
    const fixture = TestBed.createComponent(LoginPage);
    const page = fixture.componentInstance;
    expect(page.form.getRawValue()).toEqual({ email: '', password: '' });
  });

  it('does not submit when form is empty', () => {
    const fixture = TestBed.createComponent(LoginPage);
    const page = fixture.componentInstance;
    page.submit();
    expect(authMock.login).not.toHaveBeenCalled();
  });

  it('sets error on failed login', () => {
    authMock.login.mockReturnValue(throwError(() => new Error('fail')));
    const fixture = TestBed.createComponent(LoginPage);
    const page = fixture.componentInstance;
    page.form.setValue({ email: 'a@b.ml', password: 'password' });
    page.submit();
    expect(page.error()).toBe(true);
    expect(page.loading()).toBe(false);
  });

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
