import { Component, inject, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateInviteResponse, LectureInviteResponse, LectureStateResponse } from '../lecture.service';
import { InstructorWorkspaceService } from './application/instructor-workspace.service';

@Component({
  selector: 'app-instructor-home',
  imports: [ReactiveFormsModule],
  templateUrl: './instructor-home.html',
  styleUrl: './instructor-home.css',
})
export class InstructorHome {
  private readonly workspaceService = inject(InstructorWorkspaceService);

  protected readonly status = signal('Ready');
  protected readonly selectedLectureId = signal('');
  protected readonly lectureState = signal<LectureStateResponse | null>(null);
  protected readonly lastCreatedInvite = signal<CreateInviteResponse | null>(null);
  protected readonly invites = signal<LectureInviteResponse[]>([]);

  readonly createLectureForm = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  readonly addQuestionForm = new FormGroup({
    prompt: new FormControl('', [Validators.required, Validators.minLength(5)]),
    modelAnswer: new FormControl('', [Validators.required, Validators.minLength(3)]),
    timeLimitSeconds: new FormControl(60, [Validators.required, Validators.min(10)]),
  });

  async createLecture() {
    if (this.createLectureForm.invalid) {
      return;
    }

    let lectureId = '';

    try {
      lectureId = await this.workspaceService.createLecture(this.createLectureForm.value.title!);
      this.selectedLectureId.set(lectureId);
      this.lastCreatedInvite.set(null);
      this.invites.set([]);
    } catch {
      this.status.set('Could not create lecture. Please retry.');
      return;
    }

    const refreshed = await this.refreshLectureState({ preserveStatusOnError: true });
    await this.refreshInvites({ preserveStatusOnError: true });
    if (refreshed) {
      this.status.set(`Lecture created: ${lectureId}`);
      return;
    }

    this.status.set(`Lecture created: ${lectureId}, but latest state could not be loaded.`);
  }

  async addQuestion() {
    const lectureId = this.selectedLectureId();
    if (!lectureId || this.addQuestionForm.invalid) {
      return;
    }

    try {
      await this.workspaceService.addQuestion(lectureId, {
        prompt: this.addQuestionForm.value.prompt!,
        modelAnswer: this.addQuestionForm.value.modelAnswer!,
        timeLimitSeconds: Number(this.addQuestionForm.value.timeLimitSeconds!),
      });
      this.addQuestionForm.reset({ prompt: '', modelAnswer: '', timeLimitSeconds: 60 });
    } catch {
      this.status.set('Could not add question. Please retry.');
      return;
    }

    const refreshed = await this.refreshLectureState({ preserveStatusOnError: true });
    this.status.set(refreshed ? 'Question added' : 'Question added, but latest state could not be loaded.');
  }

  async unlockNextQuestion() {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return;
    }

    try {
      await this.workspaceService.unlockNextQuestion(lectureId);
    } catch {
      this.status.set('Could not unlock next question. Please retry.');
      return;
    }

    const refreshed = await this.refreshLectureState({ preserveStatusOnError: true });
    this.status.set(refreshed ? 'Unlocked next question' : 'Unlocked next question, but latest state could not be loaded.');
  }

  async unlockQuestion(questionId: string) {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return;
    }

    try {
      await this.workspaceService.unlockQuestion(lectureId, questionId);
    } catch {
      this.status.set('Could not unlock selected question. Please retry.');
      return;
    }

    const refreshed = await this.refreshLectureState({ preserveStatusOnError: true });
    this.status.set(
      refreshed
        ? `Unlocked question ${questionId}`
        : `Unlocked question ${questionId}, but latest state could not be loaded.`,
    );
  }

  async refreshLectureState(options?: { preserveStatusOnError?: boolean }) {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return false;
    }

    try {
      const state = await this.workspaceService.getLectureState(lectureId);
      this.lectureState.set(state);
      return true;
    } catch {
      if (!options?.preserveStatusOnError) {
        this.status.set('Could not refresh lecture state. Please retry.');
      }
      return false;
    }
  }

  async createInvite() {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return;
    }

    try {
      const invite = await this.workspaceService.createInvite(lectureId);
      this.lastCreatedInvite.set(invite);
    } catch {
      this.status.set('Could not create invite. Please retry.');
      return;
    }

    const refreshed = await this.refreshInvites({ preserveStatusOnError: true });
    this.status.set(refreshed ? 'Invite created' : 'Invite created, but invite list could not be loaded.');
  }

  async revokeInvite(inviteId: string) {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return;
    }

    try {
      await this.workspaceService.revokeInvite(lectureId, inviteId);
    } catch {
      this.status.set('Could not revoke invite. Please retry.');
      return;
    }

    const refreshed = await this.refreshInvites({ preserveStatusOnError: true });
    this.status.set(refreshed ? 'Invite revoked' : 'Invite revoked, but invite list could not be loaded.');
  }

  async refreshInvites(options?: { preserveStatusOnError?: boolean }) {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return false;
    }

    try {
      const invites = await this.workspaceService.listInvites(lectureId);
      this.invites.set(invites);
      return true;
    } catch {
      if (!options?.preserveStatusOnError) {
        this.status.set('Could not refresh invites. Please retry.');
      }
      return false;
    }
  }

  protected inviteDisplayStatus(invite: LectureInviteResponse): string {
    if (invite.active) {
      return 'active';
    }
    if (invite.revokedAt) {
      return 'revoked';
    }
    return 'expired';
  }
}
