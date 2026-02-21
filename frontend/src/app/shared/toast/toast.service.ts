import { Injectable, signal } from '@angular/core';

export type ToastLevel = 'info' | 'success' | 'warning' | 'error';

export type ToastMessage = {
  id: number;
  level: ToastLevel;
  text: string;
};

@Injectable({ providedIn: 'root' })
export class ToastService {
  private nextId = 1;
  private readonly _messages = signal<ToastMessage[]>([]);

  readonly messages = this._messages.asReadonly();

  show(level: ToastLevel, text: string, durationMs = 4200) {
    const id = this.nextId++;
    this._messages.update((messages) => [...messages, { id, level, text }]);
    window.setTimeout(() => this.dismiss(id), durationMs);
  }

  dismiss(id: number) {
    this._messages.update((messages) => messages.filter((message) => message.id !== id));
  }
}
