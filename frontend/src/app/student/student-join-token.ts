import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { StudentWorkspaceService } from './application/student-workspace.service';

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

  protected readonly status = signal('Preparing invite join...');
  protected readonly errorMessage = signal('');

  async ngOnInit() {
    const token = this.route.snapshot.paramMap.get('token')?.trim();
    if (!token) {
      this.status.set('Invite link is invalid');
      this.errorMessage.set('Missing invite token. Ask your instructor for a new link.');
      return;
    }

    this.status.set('Joining lecture...');
    this.errorMessage.set('');

    try {
      const result = await this.workspaceService.joinLectureByToken(token);
      await this.router.navigate(['/student'], {
        queryParams: {
          lectureId: result.lectureId,
          autoJoined: '1',
          alreadyEnrolled: result.alreadyEnrolled ? '1' : '0',
        },
      });
    } catch (error: any) {
      const errorCode = error?.error?.code;
      if (errorCode === 'INVITE_NOT_FOUND') {
        this.status.set('Invite unavailable');
        this.errorMessage.set('This invite is invalid, revoked, or expired. Ask your instructor for a fresh link.');
        return;
      }

      this.status.set('Could not join lecture');
      this.errorMessage.set('Please retry from the same link. If it keeps failing, ask your instructor for a new invite.');
    }
  }
}
