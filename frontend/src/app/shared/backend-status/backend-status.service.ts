import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { expand, Observable, of, switchMap, timer } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

@Injectable({ providedIn: 'root' })
export class BackendStatusService {
  private http = inject(HttpClient);

  private _backendUp = signal<boolean>(true);
  public readonly backendUp = this._backendUp.asReadonly();

  private _lastChange = signal<Date>(new Date());
  public readonly lastChange = this._lastChange.asReadonly();

  constructor() {
    // Checks the backend status every X seconds based on current status (longer when up)
    this.checkBackendStatus().pipe(
      expand(() => {
        const delay = this._backendUp() ? 5000 : 1000;
        return timer(delay).pipe(
          switchMap(() => this.checkBackendStatus()),
        );
      }),
    ).subscribe();
  }

  checkBackendStatus(): Observable<boolean> {
    return this.http.get('/health', { responseType: 'text' }).pipe(
      map(() => true),
      catchError(() => of(false)),
      tap((isUp) => {
        console.debug("Backend health : ", isUp ? 'UP' : 'DOWN');
        if (this._backendUp() !== isUp) {
          this._lastChange.set(new Date());
        }
        this._backendUp.set(isUp);
      }),
    );
  }
}
