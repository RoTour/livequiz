import { Component, inject } from '@angular/core';
import { ToastService } from './toast.service';

@Component({
  selector: 'app-toast-center',
  templateUrl: './toast-center.html',
})
export class ToastCenter {
  protected readonly toastService = inject(ToastService);

  protected dismiss(id: number) {
    this.toastService.dismiss(id);
  }
}
