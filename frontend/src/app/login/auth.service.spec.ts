import { HttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom, of } from 'rxjs';
import { vi } from 'vitest';
import { LocalStorageKeys } from '../../LocalStorageKeys';
import { AuthService } from './auth.service';

function createToken(claims: Record<string, unknown>): string {
  const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
  const payload = btoa(JSON.stringify({ sub: 'user-1', ...claims }));
  return `${header}.${payload}.signature`;
}

describe('AuthService', () => {
  const post = vi.fn();

  beforeEach(() => {
    localStorage.clear();
    post.mockReset();
    TestBed.configureTestingModule({
      providers: [
        {
          provide: HttpClient,
          useValue: {
            post,
          },
        },
      ],
    });
  });

  it('derives instructor role and route from token', () => {
    localStorage.setItem(LocalStorageKeys.authorization, createToken({ role: 'INSTRUCTOR' }));
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.role()).toBe('INSTRUCTOR');
    expect(service.routeForCurrentUser()).toBe('/instructor');
  });

  it('derives student claims from token', () => {
    localStorage.setItem(
      LocalStorageKeys.authorization,
      createToken({ role: 'STUDENT', anonymous: true, emailVerified: false }),
    );
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(true);
    expect(service.role()).toBe('STUDENT');
    expect(service.isAnonymousStudent()).toBe(true);
    expect(service.isStudentEmailVerified()).toBe(false);
    expect(service.routeForCurrentUser()).toBe('/student/lectures');
  });

  it('falls back to login route for malformed token payload', () => {
    localStorage.setItem(LocalStorageKeys.authorization, 'not.a.valid.jwt');
    const service = TestBed.inject(AuthService);

    expect(service.isAuthenticated()).toBe(false);
    expect(service.role()).toBeNull();
    expect(service.routeForCurrentUser()).toBe('/auth/login');
  });

  it('issues anonymous student token and stores it', async () => {
    const anonymousToken = createToken({ role: 'STUDENT', anonymous: true, emailVerified: false });
    post.mockReturnValue(of({ token: anonymousToken }));
    const service = TestBed.inject(AuthService);

    await service.ensureStudentSession();

    expect(post).toHaveBeenCalledWith('/api/auth/anonymous', {});
    expect(service.role()).toBe('STUDENT');
    expect(service.isAnonymousStudent()).toBe(true);
    expect(service.isStudentEmailVerified()).toBe(false);
  });

  it('reuses existing student token in ensureStudentSession', async () => {
    localStorage.setItem(
      LocalStorageKeys.authorization,
      createToken({ role: 'STUDENT', anonymous: false, emailVerified: true }),
    );
    const service = TestBed.inject(AuthService);

    await service.ensureStudentSession();

    expect(post).not.toHaveBeenCalled();
    expect(service.isStudentEmailVerified()).toBe(true);
  });

  it('replaces token after successful verify-email', async () => {
    localStorage.setItem(
      LocalStorageKeys.authorization,
      createToken({ role: 'STUDENT', anonymous: true, emailVerified: false }),
    );
    const verifiedToken = createToken({ role: 'STUDENT', anonymous: false, emailVerified: true });
    post.mockReturnValue(of({ token: verifiedToken }));
    const service = TestBed.inject(AuthService);

    await firstValueFrom(service.verifyStudentEmail('verification-token'));

    expect(post).toHaveBeenCalledWith('/api/auth/students/verify-email', {
      token: 'verification-token',
    });
    expect(service.isAnonymousStudent()).toBe(false);
    expect(service.isStudentEmailVerified()).toBe(true);
  });

  it('calls register-email endpoint with provided school email', async () => {
    post.mockReturnValue(of({ status: 'VERIFICATION_EMAIL_SENT_IF_ALLOWED' }));
    const service = TestBed.inject(AuthService);

    await firstValueFrom(service.registerStudentEmail('student@ynov.com'));

    expect(post).toHaveBeenCalledWith('/api/auth/students/register-email', {
      email: 'student@ynov.com',
    });
  });

  it('calls resend-verification endpoint with optional email', async () => {
    post.mockReturnValue(of({ status: 'VERIFICATION_EMAIL_SENT_IF_ALLOWED' }));
    const service = TestBed.inject(AuthService);

    await firstValueFrom(service.resendStudentVerification('student@ynov.com'));

    expect(post).toHaveBeenCalledWith('/api/auth/students/resend-verification', {
      email: 'student@ynov.com',
    });
  });

  it('clears session state on logout', () => {
    localStorage.setItem(
      LocalStorageKeys.authorization,
      createToken({ role: 'STUDENT', anonymous: true, emailVerified: false }),
    );
    const service = TestBed.inject(AuthService);

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.role()).toBeNull();
    expect(localStorage.getItem(LocalStorageKeys.authorization)).toBeNull();
  });
});
