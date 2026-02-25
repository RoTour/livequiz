import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StudentWorkspaceService } from './application/student-workspace.service';
import { AuthService } from '../login/auth.service';

@Component({
  selector: 'app-student-join-token',
  imports: [],
  templateUrl: './student-join-token.html',
  styleUrl: './student-join-token.css',
})
export class StudentJoinToken implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly workspaceService = inject(StudentWorkspaceService);
  private readonly authService = inject(AuthService);

  protected readonly status = signal('Preparing invite join...');
  protected readonly errorMessage = signal('');

  async ngOnInit() {
    const token = this.route.snapshot.paramMap.get('token')?.trim();
    if (!token) {
      this.status.set('Invite link is invalid');
      this.errorMessage.set('Missing invite token. Ask your instructor for a new link.');
      return;
    }

    if (this.authService.role() === 'INSTRUCTOR') {
      this.status.set('Instructor session detected');
      this.errorMessage.set('Invite links are for students. Redirecting to instructor workspace.');
      await this.router.navigate(['/instructor/lectures']);
      return;
    }

    try {
      this.status.set('Preparing student session...');
      this.errorMessage.set('');
      await this.authService.ensureStudentSession();
    } catch {
      this.status.set('Could not start student session');
      this.errorMessage.set('Please reload this invite link to try again.');
      return;
    }

    this.status.set('Joining lecture...');
    this.errorMessage.set('');

    try {
      const result = await this.workspaceService.joinLectureByToken(token);
      await this.router.navigate(['/student/lectures', result.lectureId]);
    } catch (error: any) {
      const errorCode = error?.error?.code;
      if (errorCode === 'INVITE_NOT_FOUND') {
        this.status.set('Invite unavailable');
        this.errorMessage.set('This invite token is invalid. Ask your instructor for a fresh link.');
        return;
      }
      if (errorCode === 'INVITE_REVOKED') {
        this.status.set('Invite revoked');
        this.errorMessage.set('This invite has been revoked. Ask your instructor for a new invite.');
        return;
      }
      if (errorCode === 'INVITE_EXPIRED') {
        this.status.set('Invite expired');
        this.errorMessage.set('This invite has expired. Ask your instructor for a new invite.');
        return;
      }

      this.status.set('Could not join lecture');
      this.errorMessage.set('Please retry from the same link. If it keeps failing, ask your instructor for a new invite.');
    }
  }
}
