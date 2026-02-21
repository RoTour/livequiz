import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  CreateInviteResponse,
  LectureInviteResponse,
  LectureStateResponse,
  QuestionAnalyticsResponse,
  StudentAnswerHistoryResponse,
} from '../lecture.service';
import { InstructorWorkspaceService } from './application/instructor-workspace.service';
import { QuestionFlowPanel } from './components/question-flow-panel/question-flow-panel';
import { InviteManagementPanel } from './components/invite-management-panel/invite-management-panel';
import { LectureStatePanel } from './components/lecture-state-panel/lecture-state-panel';

@Component({
  selector: 'app-instructor-home',
  imports: [
    ReactiveFormsModule,
    QuestionFlowPanel,
    InviteManagementPanel,
    LectureStatePanel,
  ],
  templateUrl: './instructor-home.html',
  styleUrl: './instructor-home.css',
})
export class InstructorHome implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly workspaceService = inject(InstructorWorkspaceService);

  protected readonly status = signal('Ready');
  protected readonly selectedLectureId = signal('');
  protected readonly lectureState = signal<LectureStateResponse | null>(null);
  protected readonly questionAnalytics = signal<QuestionAnalyticsResponse[]>([]);
  protected readonly analyticsLoading = signal(false);
  protected readonly analyticsError = signal('');
  protected readonly selectedHistoryQuestionId = signal('');
  protected readonly questionHistory = signal<StudentAnswerHistoryResponse[]>([]);
  protected readonly questionHistoryLoading = signal(false);
  protected readonly questionHistoryError = signal('');
  protected readonly lastCreatedInvite = signal<CreateInviteResponse | null>(null);
  protected readonly invites = signal<LectureInviteResponse[]>([]);

  readonly addQuestionForm = new FormGroup({
    prompt: new FormControl('', [Validators.required, Validators.minLength(5)]),
    modelAnswer: new FormControl('', [Validators.required, Validators.minLength(3)]),
    timeLimitSeconds: new FormControl(60, [Validators.required, Validators.min(10)]),
  });

  async ngOnInit() {
    await this.loadLectureContext(this.route.snapshot.paramMap.get('lectureId'));
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((paramMap) => {
      void this.loadLectureContext(paramMap.get('lectureId'));
    });
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
    await this.refreshQuestionAnalytics({ preserveStatusOnError: true });
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
    await this.refreshQuestionAnalytics({ preserveStatusOnError: true });
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
    await this.refreshQuestionAnalytics({ preserveStatusOnError: true });
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

  async refreshQuestionAnalytics(options?: { preserveStatusOnError?: boolean }) {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      this.questionAnalytics.set([]);
      this.analyticsError.set('');
      return false;
    }

    this.analyticsLoading.set(true);
    this.analyticsError.set('');
    try {
      const analytics = await this.workspaceService.listQuestionAnalytics(lectureId);
      this.questionAnalytics.set(analytics);
      return true;
    } catch {
      this.questionAnalytics.set([]);
      this.analyticsError.set('Could not load analytics. Question and invite actions are still available.');
      if (!options?.preserveStatusOnError) {
        this.status.set('Could not refresh analytics. Please retry.');
      }
      return false;
    } finally {
      this.analyticsLoading.set(false);
    }
  }

  async openQuestionAnswerHistory(questionId: string) {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return;
    }

    this.selectedHistoryQuestionId.set(questionId);
    this.questionHistory.set([]);
    this.questionHistoryError.set('');
    this.questionHistoryLoading.set(true);

    try {
      const history = await this.workspaceService.listQuestionAnswerHistory(lectureId, questionId);
      this.questionHistory.set(history);
    } catch {
      this.questionHistoryError.set('Could not load student answer history for this question.');
    } finally {
      this.questionHistoryLoading.set(false);
    }
  }

  closeQuestionAnswerHistory() {
    this.selectedHistoryQuestionId.set('');
    this.questionHistory.set([]);
    this.questionHistoryError.set('');
    this.questionHistoryLoading.set(false);
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

  private async loadLectureContext(lectureId: string | null) {
    const normalizedLectureId = lectureId?.trim() ?? '';
    if (!normalizedLectureId) {
      this.selectedLectureId.set('');
      this.lectureState.set(null);
      this.questionAnalytics.set([]);
      this.analyticsError.set('');
      this.closeQuestionAnswerHistory();
      this.lastCreatedInvite.set(null);
      this.invites.set([]);
      this.status.set('Lecture not selected. Return to the lecture list.');
      return;
    }

    this.selectedLectureId.set(normalizedLectureId);
    this.questionAnalytics.set([]);
    this.analyticsError.set('');
    this.closeQuestionAnswerHistory();
    this.lastCreatedInvite.set(null);
    this.invites.set([]);

    const refreshed = await this.refreshLectureState({ preserveStatusOnError: true });
    await this.refreshQuestionAnalytics({ preserveStatusOnError: true });
    await this.refreshInvites({ preserveStatusOnError: true });
    this.status.set(
      refreshed ? `Loaded lecture: ${normalizedLectureId}` : `Could not load lecture ${normalizedLectureId}.`,
    );
  }
}
