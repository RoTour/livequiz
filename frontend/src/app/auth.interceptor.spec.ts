import { HttpRequest, HttpResponse } from '@angular/common/http';
import { of } from 'rxjs';
import { LocalStorageKeys } from '../LocalStorageKeys';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('adds Authorization header on protected endpoints', () => {
    localStorage.setItem(LocalStorageKeys.authorization, 'token-123');
    const request = new HttpRequest('GET', '/api/lectures');
    let authorizationHeader: string | null | undefined;

    authInterceptor(request, (nextRequest) => {
      authorizationHeader = nextRequest.headers.get('Authorization');
      return of(new HttpResponse({ status: 200 }));
    }).subscribe();

    expect(authorizationHeader).toBe('Bearer token-123');
  });

  it('does not attach Authorization header for login endpoint', () => {
    localStorage.setItem(LocalStorageKeys.authorization, 'token-123');
    const request = new HttpRequest('POST', '/api/auth/login', {
      username: 'student',
      password: 'password',
    });
    let hasAuthorization = false;

    authInterceptor(request, (nextRequest) => {
      hasAuthorization = nextRequest.headers.has('Authorization');
      return of(new HttpResponse({ status: 200 }));
    }).subscribe();

    expect(hasAuthorization).toBe(false);
  });

  it('does not attach Authorization header for anonymous bootstrap endpoint', () => {
    localStorage.setItem(LocalStorageKeys.authorization, 'token-123');
    const request = new HttpRequest('POST', '/api/auth/anonymous', {});
    let hasAuthorization = false;

    authInterceptor(request, (nextRequest) => {
      hasAuthorization = nextRequest.headers.has('Authorization');
      return of(new HttpResponse({ status: 200 }));
    }).subscribe();

    expect(hasAuthorization).toBe(false);
  });

  it('does not attach Authorization header for verify-email endpoint', () => {
    localStorage.setItem(LocalStorageKeys.authorization, 'token-123');
    const request = new HttpRequest('POST', '/api/auth/students/verify-email', { token: 'verify-token' });
    let hasAuthorization = false;

    authInterceptor(request, (nextRequest) => {
      hasAuthorization = nextRequest.headers.has('Authorization');
      return of(new HttpResponse({ status: 200 }));
    }).subscribe();

    expect(hasAuthorization).toBe(false);
  });
});
