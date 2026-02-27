import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { firstValueFrom, tap } from 'rxjs';
import { LocalStorageKeys } from '../../LocalStorageKeys';
import { routeForRole } from '../auth/application/role-routing';
import { isUserRole, UserRole } from '../auth/domain/user-role';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private _token = signal<null | string>(localStorage.getItem(LocalStorageKeys.authorization));
  readonly token = this._token.asReadonly();
  readonly claims = computed<Record<string, unknown> | null>(() => this.extractClaims(this._token()));
  readonly role = computed<UserRole | null>(() => {
    const role = this.claims()?.['role'];
    return isUserRole(role) ? role : null;
  });
  readonly isAuthenticated = computed(() => Boolean(this._token()) && this.role() !== null);
  readonly isAnonymousStudent = computed(() => {
    return this.role() === 'STUDENT' && this.claims()?.['anonymous'] === true;
  });
  readonly isStudentEmailVerified = computed(() => {
    if (this.role() !== 'STUDENT') {
      return false;
    }
    return this.claims()?.['emailVerified'] === true;
  });

  login(identifier: string, password: string) {
    const normalizedIdentifier = identifier.trim();
    const payload = normalizedIdentifier.includes('@')
      ? { email: normalizedIdentifier, password }
      : { username: normalizedIdentifier, password };

    return this.http.post<{ token: string }>('/api/auth/login', payload).pipe(
      tap((response) => {
        this.setTokenState(response.token);
      }),
    );
  }

  issueAnonymousStudentToken() {
    return this.http.post<{ token: string }>('/api/auth/anonymous', {}).pipe(
      tap((response) => {
        this.setTokenState(response.token);
      }),
    );
  }

  registerStudentEmail(email: string) {
    return this.http.post<{ status: string }>('/api/auth/students/register-email', { email });
  }

  resendStudentVerification(email?: string) {
    const payload = email ? { email } : {};
    return this.http.post<{ status: string }>('/api/auth/students/resend-verification', payload);
  }

  verifyStudentEmail(token: string) {
    return this.http.post<{ token: string }>('/api/auth/students/verify-email', { token }).pipe(
      tap((response) => {
        this.setTokenState(response.token);
      }),
    );
  }

  async ensureStudentSession() {
    const role = this.role();
    if (role === 'INSTRUCTOR') {
      return;
    }

    if (role === 'STUDENT' && this._token()) {
      return;
    }

    await firstValueFrom(this.issueAnonymousStudentToken());
  }

  logout() {
    this.setTokenState(null);
  }

  routeForCurrentUser(): string {
    return routeForRole(this.role());
  }

  private setTokenState(token: string | null) {
    if (token) {
      localStorage.setItem(LocalStorageKeys.authorization, token);
      this._token.set(token);
      return;
    }

    localStorage.removeItem(LocalStorageKeys.authorization);
    this._token.set(null);
  }

  private extractClaims(token: string | null): Record<string, unknown> | null {
    if (!token) {
      return null;
    }

    const payload = token.split('.')[1];
    if (!payload) {
      return null;
    }

    try {
      const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
      const decodedPayload = atob(normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, '='));
      return JSON.parse(decodedPayload) as Record<string, unknown>;
    } catch {
      return null;
    }
  }
}
