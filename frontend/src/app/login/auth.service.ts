import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { tap } from 'rxjs';
import { LocalStorageKeys } from '../../LocalStorageKeys';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private _token = signal<null | string>(localStorage.getItem(LocalStorageKeys.authorization));
  readonly token = this._token.asReadonly();

  login(username: string, password: string) {
    return this.http.post<{ token: string }>('/api/auth/login', { username, password }).pipe(
      tap((response) => {
        const token = response.token;
        localStorage.setItem(LocalStorageKeys.authorization, token);
        this._token.set(token);
      }),
    );
  }
}
