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
  showInstructorLogin = signal(false);
  authErrorMessage = signal('');
  studentMagicLinkStatusMessage = signal('');
  studentMagicLinkErrorMessage = signal('');
  form = new FormGroup({
    identifier: new FormControl('instructor@ynov.com', Validators.required),
    password: new FormControl('password', [
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(64),
    ]),
  });
  studentMagicLinkForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
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
        this.authErrorMessage.set(
          'Invalid instructor credentials. If you are a student, use the student access link form.',
        );
        this.form.enable();
      },
    });
  }

  toggleInstructorLogin() {
    this.showInstructorLogin.update((current) => !current);
  }

  closeInstructorLogin() {
    this.showInstructorLogin.set(false);
  }

  requestStudentMagicLink() {
    if (this.studentMagicLinkForm.invalid || this.studentMagicLinkForm.disabled) {
      this.studentMagicLinkForm.markAllAsTouched();
      return;
    }

    const email = this.studentMagicLinkForm.value.email?.trim();
    if (!email) {
      return;
    }

    this.studentMagicLinkStatusMessage.set('');
    this.studentMagicLinkErrorMessage.set('');
    this.studentMagicLinkForm.disable();

    this.authService.requestStudentMagicLogin(email).subscribe({
      next: () => {
        this.studentMagicLinkStatusMessage.set(
          'If allowed, we sent you a student access link. It also verifies your email when needed.',
        );
        this.studentMagicLinkForm.enable();
      },
      error: (error) => {
        this.studentMagicLinkErrorMessage.set(this.resolveStudentMagicLinkError(error?.error?.code));
        this.studentMagicLinkForm.enable();
      },
    });
  }

  private resolveStudentMagicLinkError(errorCode: string | undefined): string {
    if (errorCode === 'EMAIL_REQUIRED') {
      return 'Email is required.';
    }
    if (errorCode === 'EMAIL_INVALID_FORMAT') {
      return 'Email format is invalid.';
    }
    if (errorCode === 'EMAIL_DOMAIN_NOT_ALLOWED') {
      return 'Only @ynov.com email addresses are allowed.';
    }
    if (errorCode === 'EMAIL_VERIFICATION_COOLDOWN') {
      return 'Please wait before requesting another access link.';
    }
    if (errorCode === 'EMAIL_VERIFICATION_RATE_LIMITED') {
      return 'Too many requests. Please try again later.';
    }
    return 'Could not request a student access link right now. Please retry.';
  }
}
