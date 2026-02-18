import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { BackendStatus } from './shared/backend-status/backend-status';
import { AuthService } from './login/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, BackendStatus],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly router = inject(Router);
  protected readonly authService = inject(AuthService);

  protected async logout() {
    this.authService.logout();
    await this.router.navigate(['/auth/login']);
  }
}
