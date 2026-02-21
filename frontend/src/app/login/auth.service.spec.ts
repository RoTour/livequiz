import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { LocalStorageKeys } from '../../LocalStorageKeys';
import { AuthService } from './auth.service';

function createToken(role: string): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(JSON.stringify({ sub: 'user-1', role }));
  return `${header}.${payload}.signature`;
}

describe('AuthService', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient()],
    });
  });

  it('derives instructor role and route from token', () => {
    localStorage.setItem(LocalStorageKeys.authorization, createToken('INSTRUCTOR'));
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.role()).toBe('INSTRUCTOR');
    expect(service.routeForCurrentUser()).toBe('/instructor');
  });

  it('derives student role and route from token', () => {
    localStorage.setItem(LocalStorageKeys.authorization, createToken('STUDENT'));
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.role()).toBe('STUDENT');
    expect(service.routeForCurrentUser()).toBe('/student/lectures');
  });

  it('falls back to login route for malformed token payload', () => {
    localStorage.setItem(LocalStorageKeys.authorization, 'not.a.valid.jwt');
    const service = TestBed.inject(AuthService);

    expect(service.role()).toBeNull();
    expect(service.routeForCurrentUser()).toBe('/auth/login');
  });

  it('clears session state on logout', () => {
    localStorage.setItem(LocalStorageKeys.authorization, createToken('STUDENT'));
    const service = TestBed.inject(AuthService);

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.role()).toBeNull();
    expect(localStorage.getItem(LocalStorageKeys.authorization)).toBeNull();
  });
});
