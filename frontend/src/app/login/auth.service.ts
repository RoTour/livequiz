import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { LocalStorageKeys } from '../../LocalStorageKeys';
import { routeForRole } from '../auth/application/role-routing';
import { isUserRole, UserRole } from '../auth/domain/user-role';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private _token = signal<null | string>(localStorage.getItem(LocalStorageKeys.authorization));
  readonly token = this._token.asReadonly();
  readonly role = computed<UserRole | null>(() => this.extractRole(this._token()));
  readonly isAuthenticated = computed(() => Boolean(this._token()));

  login(username: string, password: string) {
    return this.http.post<{ token: string }>('/api/auth/login', { username, password }).pipe(
      tap((response) => {
        const token = response.token;
        localStorage.setItem(LocalStorageKeys.authorization, token);
        this._token.set(token);
      }),
    );
  }

  logout() {
    localStorage.removeItem(LocalStorageKeys.authorization);
    this._token.set(null);
  }

  routeForCurrentUser(): string {
    return routeForRole(this.role());
  }

  private extractRole(token: string | null): UserRole | null {
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
      const claims = JSON.parse(decodedPayload) as Record<string, unknown>;
      const role = claims['role'];

      return isUserRole(role) ? role : null;
    } catch {
      return null;
    }
  }
}
