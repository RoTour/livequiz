import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../login/auth.service';

@Component({
  selector: 'app-student-verify-email',
  imports: [],
  templateUrl: './student-verify-email.html',
})
export class StudentVerifyEmail implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  protected readonly status = signal('Preparing secure student access...');
  protected readonly errorMessage = signal('');
  protected readonly verified = signal(false);

  async ngOnInit() {
    const token = this.route.snapshot.queryParamMap.get('token')?.trim();
    if (!token) {
      this.status.set('Access link is invalid');
      this.errorMessage.set('Missing verification token. Request a new verification email from the student workspace.');
      return;
    }

    this.status.set('Verifying your school email and restoring your session...');
    this.errorMessage.set('');

    try {
      await firstValueFrom(this.authService.verifyStudentEmail(token));
      this.verified.set(true);
      this.status.set('Student access granted');
      const navigated = await this.router.navigate(['/student/lectures']);
      if (!navigated) {
        this.errorMessage.set('Email verified, but we could not open your student workspace automatically.');
      }
    } catch (error: any) {
      const errorCode = error?.error?.code;
      if (errorCode === 'EMAIL_VERIFICATION_TOKEN_INVALID') {
        this.status.set('Verification failed');
        this.errorMessage.set('This verification link is invalid. Request a new verification email from the student workspace.');
        return;
      }
      if (errorCode === 'EMAIL_VERIFICATION_TOKEN_CONSUMED') {
        this.status.set('Verification already completed');
        this.errorMessage.set('This verification link has already been used. You can continue from your student workspace.');
        return;
      }
      if (errorCode === 'EMAIL_VERIFICATION_TOKEN_EXPIRED') {
        this.status.set('Verification link expired');
        this.errorMessage.set('This verification link has expired. Request a new verification email from the student workspace.');
        return;
      }
      if (errorCode === 'EMAIL_VERIFICATION_TOKEN_REQUIRED') {
        this.status.set('Access link is invalid');
        this.errorMessage.set('Missing verification token. Request a new verification email from the student workspace.');
        return;
      }

      this.status.set('Could not verify your student access');
      this.errorMessage.set('Please try the same verification link again.');
    }
  }

  protected async goToLogin() {
    await this.router.navigate(['/auth/login']);
  }
}
