import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { from, of, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Login } from './login';
import { AuthService } from './auth.service';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  const login = vi.fn();
  const routeForCurrentUser = vi.fn();
  const logout = vi.fn();
  const navigate = vi.fn();

  beforeEach(async () => {
    login.mockReset();
    routeForCurrentUser.mockReset();
    logout.mockReset();
    navigate.mockReset();

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        {
          provide: AuthService,
          useValue: {
            login,
            routeForCurrentUser,
            logout,
          },
        },
        {
          provide: Router,
          useValue: {
            navigate,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('routes to the role default page after successful login', async () => {
    routeForCurrentUser.mockReturnValue('/student');
    login.mockReturnValue(from(Promise.resolve({ token: 'jwt' })));
    navigate.mockResolvedValue(true);
    component.form.setValue({ username: 'student', password: 'password' });

    component.submit();
    await fixture.whenStable();

    expect(login).toHaveBeenCalledWith('student', 'password');
    expect(routeForCurrentUser).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/student']);
  });

  it('logs out and re-enables form when role target is unknown', async () => {
    routeForCurrentUser.mockReturnValue('/auth/login');
    login.mockReturnValue(from(Promise.resolve({ token: 'jwt' })));
    component.form.setValue({ username: 'user', password: 'password' });

    component.submit();
    await fixture.whenStable();

    expect(logout).toHaveBeenCalled();
    expect(navigate).not.toHaveBeenCalled();
    expect(component.form.disabled).toBe(false);
    expect(component.authErrorMessage()).toContain('Could not determine your role');
  });

  it('re-enables form when post-login navigation fails', async () => {
    routeForCurrentUser.mockReturnValue('/student');
    login.mockReturnValue(from(Promise.resolve({ token: 'jwt' })));
    navigate.mockResolvedValue(false);
    component.form.setValue({ username: 'student', password: 'password' });

    component.submit();
    await fixture.whenStable();

    expect(navigate).toHaveBeenCalledWith(['/student']);
    expect(component.form.disabled).toBe(false);
    expect(component.authErrorMessage()).toContain('Could not open your dashboard');
  });

  it('re-enables form when login fails', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    login.mockReturnValue(throwError(() => new Error('invalid credentials')));
    component.form.setValue({ username: 'instructor', password: 'password' });

    component.submit();

    expect(component.form.disabled).toBe(false);
    consoleError.mockRestore();
  });
});
