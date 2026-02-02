import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from './auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  authService = inject(AuthService);
  form = new FormGroup({
    username: new FormControl('admin', Validators.required),
    password: new FormControl('username', [
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(64),
    ]),
  });

  submit() {
    if (this.form.invalid) return;

    this.form.disable();

    this.authService.login(this.form.value.username!, 'password').subscribe({
      next: () => {
        this.form.enable();
        this.form.reset();
      },
      error: (error) => {
        console.error('Login failed:', error);
        this.form.enable();
      },
    });
  }
}
