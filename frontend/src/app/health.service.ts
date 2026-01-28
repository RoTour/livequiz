import { HttpClient } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { catchError, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HealthService {
  private http = inject(HttpClient);
  
  // Signal to hold the health status
  status = signal<string>('Checking...');

  checkHealth() {
    this.http.get<{status: string, timestamp: string}>('http://localhost:8080/health')
      .pipe(
        catchError(err => of({ status: 'DOWN', timestamp: '' }))
      )
      .subscribe(response => {
        this.status.set(response.status);
      });
  }
}
