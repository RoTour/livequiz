import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { StudentLectureSummaryResponse } from '../lecture.service';
import { StudentWorkspaceService } from './application/student-workspace.service';
import { HumanDatePipe } from '../shared/date/human-date.pipe';
import { ToastService } from '../shared/toast/toast.service';
import { AuthService } from '../login/auth.service';

@Component({
  selector: 'app-student-lecture-list',
  imports: [ReactiveFormsModule, HumanDatePipe],
  templateUrl: './student-lecture-list.html',
})
export class StudentLectureList implements OnInit {
  private readonly workspaceService = inject(StudentWorkspaceService);
  private readonly router = inject(Router);
  private readonly toastService = inject(ToastService);
  protected readonly authService = inject(AuthService);

  protected readonly status = signal('Ready');
  protected readonly loading = signal(false);
  protected readonly lectures = signal<StudentLectureSummaryResponse[]>([]);
  protected readonly joinResult = signal('');
  protected readonly emailVerificationStatus = signal('');
  protected readonly emailVerificationError = signal('');
  protected readonly emailVerificationBusy = signal(false);

  readonly joinLectureForm = new FormGroup({
    code: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]),
  });

  readonly registerEmailForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email]),
  });

  ngOnInit() {
    void this.refreshLectures();
  }

  async refreshLectures() {
    this.loading.set(true);
    try {
      const lectures = await this.workspaceService.listLectures();
      this.lectures.set(lectures);
      this.status.set('Lectures loaded');
    } catch {
      this.status.set('Could not load your lectures. Please retry.');
      this.toastService.show('error', 'Could not load your lectures. Please retry.');
    } finally {
      this.loading.set(false);
    }
  }

  async joinLecture() {
    if (this.joinLectureForm.invalid) {
      return;
    }

    try {
      const code = this.joinLectureForm.value.code!.trim().toUpperCase();
      const result = await this.workspaceService.joinLectureByCode(code);
      this.joinResult.set(result.alreadyEnrolled ? 'Already enrolled' : 'Enrolled successfully');
      this.toastService.show(
        'success',
        result.alreadyEnrolled ? 'You are already enrolled in this lecture.' : 'Lecture joined successfully.',
      );
      this.joinLectureForm.reset({ code: '' });
      await this.refreshLectures();
      await this.router.navigate(['/student/lectures', result.lectureId]);
    } catch {
      this.status.set('Could not join lecture. Verify code and retry.');
      this.toastService.show('warning', 'Could not join lecture. Verify code and retry.');
    }
  }

  async openLecture(lectureId: string) {
    await this.router.navigate(['/student/lectures', lectureId]);
  }

  async registerEmail() {
    if (this.registerEmailForm.invalid || this.emailVerificationBusy()) {
      this.registerEmailForm.markAllAsTouched();
      return;
    }

    const email = this.registerEmailForm.value.email?.trim();
    if (!email) {
      return;
    }

    this.emailVerificationBusy.set(true);
    this.emailVerificationStatus.set('');
    this.emailVerificationError.set('');

    try {
      await firstValueFrom(this.authService.registerStudentEmail(email));
      this.emailVerificationStatus.set('If allowed, a verification email has been sent.');
      this.toastService.show('info', 'If allowed, a verification email has been sent.');
    } catch (error: any) {
      const message = this.resolveEmailVerificationErrorMessage(error?.error?.code);
      this.emailVerificationError.set(message);
      this.toastService.show('warning', message);
    } finally {
      this.emailVerificationBusy.set(false);
    }
  }

  async resendVerification() {
    if (this.emailVerificationBusy()) {
      return;
    }

    const email = this.registerEmailForm.value.email?.trim();

    this.emailVerificationBusy.set(true);
    this.emailVerificationStatus.set('');
    this.emailVerificationError.set('');

    try {
      await firstValueFrom(this.authService.resendStudentVerification(email || undefined));
      this.emailVerificationStatus.set('If allowed, a verification email has been resent.');
      this.toastService.show('info', 'If allowed, a verification email has been resent.');
    } catch (error: any) {
      const message = this.resolveEmailVerificationErrorMessage(error?.error?.code);
      this.emailVerificationError.set(message);
      this.toastService.show('warning', message);
    } finally {
      this.emailVerificationBusy.set(false);
    }
  }

  private resolveEmailVerificationErrorMessage(errorCode: string | undefined): string {
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
      return 'Please wait before requesting another verification email.';
    }
    if (errorCode === 'EMAIL_VERIFICATION_RATE_LIMITED') {
      return 'Too many verification attempts. Please try again later.';
    }
    return 'Could not process email verification right now. Please retry.';
  }
}
