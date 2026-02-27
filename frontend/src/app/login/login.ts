import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from './auth.service';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  authService = inject(AuthService);
  router = inject(Router);
  route = inject(ActivatedRoute);
  authErrorMessage = signal('');
  form = new FormGroup({
    identifier: new FormControl('instructor@ynov.com', Validators.required),
    password: new FormControl('password', [
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(64),
    ]),
  });

  submit() {
    if (this.form.invalid) return;

    this.authErrorMessage.set('');
    this.form.disable();

    this.authService.login(this.form.value.identifier!, this.form.value.password!).subscribe({
      next: async () => {
        const roleRoute = this.authService.routeForCurrentUser();

        if (roleRoute === '/auth/login') {
          this.authService.logout();
          this.authErrorMessage.set('Could not determine your role. Please log in again.');
          this.form.enable();
          return;
        }

        const returnUrl = this.route.snapshot.queryParamMap.get('returnUrl');
        const navigated = returnUrl
          ? await this.router.navigateByUrl(returnUrl)
          : await this.router.navigate([roleRoute]);
        if (!navigated) {
          this.authErrorMessage.set('Could not open your dashboard. Please try again.');
          this.form.enable();
        }
      },
      error: (error) => {
        console.error('Login failed:', error);
        this.authErrorMessage.set('Invalid email or username, or password.');
        this.form.enable();
      },
    });
  }
}
