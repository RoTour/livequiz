import { Component, computed, inject, isDevMode, signal } from '@angular/core';
import { BackendStatusService } from './backend-status.service';
import { timer } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-backend-status',
  imports: [],
  templateUrl: './backend-status.html',
  styleUrl: './backend-status.css',
})
export class BackendStatus {
  devMode = isDevMode();
  backendStatus = computed(() => (this.backendService.backendUp() ? 'Backend Up' : 'Backend Down'));
  backendService = inject(BackendStatusService);

  private now = signal(new Date());
  public timeInStatus = computed(() => Math.floor((this.now().getTime() - this.backendService.lastChange().getTime()) / 1000));

  bgClass = computed(() => (this.backendService.backendUp() ? 'lq-dev-ok' : 'lq-dev-down'));

  constructor() {
    if (!this.devMode) return;

    timer(0, 1000).pipe(
      takeUntilDestroyed(),
    ).subscribe(() => {
      this.now.set(new Date());
    });
  }
}
