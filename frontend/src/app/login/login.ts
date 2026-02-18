import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  authService = inject(AuthService);
  router = inject(Router);
  form = new FormGroup({
    username: new FormControl('instructor', Validators.required),
    password: new FormControl('password', [
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(64),
    ]),
  });

  submit() {
    if (this.form.invalid) return;

    this.form.disable();

    this.authService.login(this.form.value.username!, this.form.value.password!).subscribe({
      next: async () => {
        await this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        console.error('Login failed:', error);
        this.form.enable();
      },
    });
  }
}
