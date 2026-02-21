import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NextQuestionResponse, StudentAnswerStatusResponse } from '../lecture.service';
import { StudentWorkspaceService } from './application/student-workspace.service';
import { AnswerFlowPanel } from './components/answer-flow-panel/answer-flow-panel';
import { ToastService } from '../shared/toast/toast.service';

@Component({
  selector: 'app-student-lecture-room',
  imports: [ReactiveFormsModule, AnswerFlowPanel],
  templateUrl: './student-lecture-room.html',
})
export class StudentLectureRoom implements OnInit {
  private static readonly POLL_INTERVAL_MS = 8000;
  private static readonly MANUAL_RELOAD_COOLDOWN_MS = 1500;

  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly workspaceService = inject(StudentWorkspaceService);
  private readonly toastService = inject(ToastService);
  private pollTimerId: number | null = null;
  private manualReloadLocked = false;

  protected readonly status = signal('Ready');
  protected readonly selectedLectureId = signal('');
  protected readonly nextQuestion = signal<NextQuestionResponse | null>(null);
  protected readonly answerStatuses = signal<StudentAnswerStatusResponse[]>([]);
  protected readonly cooldownMessage = signal('');
  protected readonly manualReloadDisabled = signal(false);

  readonly submitAnswerForm = new FormGroup({
    answerText: new FormControl('', [Validators.required, Validators.minLength(2)]),
  });

  async ngOnInit() {
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((paramMap) => {
      void this.loadLectureContext(paramMap.get('lectureId'));
    });
  }

  async loadNextQuestion() {
    await this.loadNextQuestionWithOptions({ manual: false });
  }

  async manualReload() {
    if (this.manualReloadLocked) {
      return;
    }
    this.manualReloadLocked = true;
    this.manualReloadDisabled.set(true);
    window.setTimeout(() => {
      this.manualReloadLocked = false;
      this.manualReloadDisabled.set(false);
    }, StudentLectureRoom.MANUAL_RELOAD_COOLDOWN_MS);
    await this.loadNextQuestionWithOptions({ manual: true });
  }

  async submitAnswer() {
    const lectureId = this.selectedLectureId();
    const currentQuestion = this.nextQuestion();
    if (!lectureId || !currentQuestion || !currentQuestion.hasQuestion || this.submitAnswerForm.invalid) {
      return;
    }

    this.cooldownMessage.set('');

    try {
      const result = await this.workspaceService.submitAnswer(lectureId, {
        questionId: currentQuestion.questionId,
        answerText: this.submitAnswerForm.value.answerText!,
      });
      this.submitAnswerForm.reset({ answerText: '' });
      this.status.set('Answer submitted');
      this.toastService.show('success', `Answer submitted (${this.answerStatusLabel(result.answerStatus)}).`);
      await this.loadAnswerStatuses(lectureId);
      await this.loadNextQuestionWithOptions({ manual: false });
    } catch (error: any) {
      if (error?.status === 429) {
        const retryAfter = error?.error?.details?.retryAfterSeconds;
        this.cooldownMessage.set(
          retryAfter ? `Cooldown active: retry in ${retryAfter}s` : 'Cooldown active. Please retry later.',
        );
        this.toastService.show('warning', 'Submission cooldown active. Please retry in a moment.');
        return;
      }
      this.status.set('Could not submit answer. Please retry.');
      this.toastService.show('error', 'Could not submit answer. Please retry.');
    }
  }

  private async loadNextQuestionWithOptions(options: { manual: boolean }) {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return;
    }

    try {
      const nextQuestion = await this.workspaceService.getNextQuestion(lectureId);
      this.nextQuestion.set(nextQuestion);
      this.cooldownMessage.set('');
      this.status.set(nextQuestion.hasQuestion ? 'Question loaded' : 'No unlocked pending question');
      this.configurePolling(nextQuestion.hasQuestion);
      if (!nextQuestion.hasQuestion && options.manual) {
        this.toastService.show('info', 'No unlocked question yet. We will keep checking automatically.');
      }
    } catch (error: any) {
      const errorCode = error?.error?.code;
      if (errorCode === 'LECTURE_ENROLLMENT_REQUIRED') {
        this.status.set('Enrollment required before loading questions.');
        this.toastService.show('warning', 'Enrollment required before loading questions.');
        return;
      }
      this.status.set('Could not load next question. Please retry.');
      this.toastService.show('error', 'Could not load next question. Please retry.');
    }
  }

  private async loadLectureContext(lectureId: string | null) {
    this.stopPolling();

    const normalizedLectureId = lectureId?.trim() ?? '';
    if (!normalizedLectureId) {
      this.selectedLectureId.set('');
      this.nextQuestion.set(null);
      this.answerStatuses.set([]);
      this.submitAnswerForm.reset({ answerText: '' });
      this.cooldownMessage.set('');
      this.status.set('Lecture not selected. Return to your lecture list.');
      return;
    }

    this.selectedLectureId.set(normalizedLectureId);
    this.nextQuestion.set(null);
    this.answerStatuses.set([]);
    this.submitAnswerForm.reset({ answerText: '' });
    this.cooldownMessage.set('');
    await this.loadAnswerStatuses(normalizedLectureId);
    await this.loadNextQuestionWithOptions({ manual: false });
  }

  private async loadAnswerStatuses(lectureId: string) {
    try {
      const statuses = await this.workspaceService.getAnswerStatuses(lectureId);
      this.answerStatuses.set(statuses);
    } catch {
      this.toastService.show('warning', 'Could not load answer statuses.');
    }
  }

  private configurePolling(hasQuestion: boolean) {
    if (hasQuestion) {
      this.stopPolling();
      return;
    }
    if (this.pollTimerId !== null) {
      return;
    }

    this.pollTimerId = window.setInterval(() => {
      void this.loadNextQuestionWithOptions({ manual: false });
    }, StudentLectureRoom.POLL_INTERVAL_MS);
  }

  private stopPolling() {
    if (this.pollTimerId !== null) {
      window.clearInterval(this.pollTimerId);
      this.pollTimerId = null;
    }
  }

  private answerStatusLabel(status: string): string {
    return status
      .toLowerCase()
      .replaceAll('_', ' ')
      .replace(/^./, (letter) => letter.toUpperCase());
  }

  ngOnDestroy() {
    this.stopPolling();
  }
}
