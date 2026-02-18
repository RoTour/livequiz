import { Component, signal, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';
import {
  CreateInviteResponse,
  LectureService,
  LectureStateResponse,
  NextQuestionResponse,
} from '../lecture.service';

@Component({
  selector: 'app-dashboard',
  imports: [ReactiveFormsModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard {
  private readonly lectureService = inject(LectureService);

  protected readonly status = signal('Ready');
  protected readonly createdLectureId = signal('');
  protected readonly lectureState = signal<LectureStateResponse | null>(null);
  protected readonly invite = signal<CreateInviteResponse | null>(null);
  protected readonly joinResult = signal('');
  protected readonly nextQuestion = signal<NextQuestionResponse | null>(null);
  protected readonly cooldownMessage = signal('');

  readonly createLectureForm = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  readonly addQuestionForm = new FormGroup({
    prompt: new FormControl('', [Validators.required, Validators.minLength(5)]),
    modelAnswer: new FormControl('', [Validators.required, Validators.minLength(3)]),
    timeLimitSeconds: new FormControl(60, [Validators.required, Validators.min(10)]),
  });

  readonly joinLectureForm = new FormGroup({
    code: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]),
  });

  readonly submitAnswerForm = new FormGroup({
    answerText: new FormControl('', [Validators.required, Validators.minLength(2)]),
  });

  async createLecture() {
    if (this.createLectureForm.invalid) {
      return;
    }

    const title = this.createLectureForm.value.title!;
    const response = await firstValueFrom(this.lectureService.create({ title }));
    this.createdLectureId.set(response.lectureId);
    this.status.set(`Lecture created: ${response.lectureId}`);
    await this.refreshLectureState();
  }

  async addQuestion() {
    const lectureId = this.createdLectureId();
    if (!lectureId || this.addQuestionForm.invalid) {
      return;
    }

    await firstValueFrom(
      this.lectureService.addQuestion(lectureId, {
        prompt: this.addQuestionForm.value.prompt!,
        modelAnswer: this.addQuestionForm.value.modelAnswer!,
        timeLimitSeconds: Number(this.addQuestionForm.value.timeLimitSeconds!),
      }),
    );

    this.addQuestionForm.reset({ prompt: '', modelAnswer: '', timeLimitSeconds: 60 });
    this.status.set('Question added');
    await this.refreshLectureState();
  }

  async unlockNextQuestion() {
    const lectureId = this.createdLectureId();
    if (!lectureId) {
      return;
    }

    await firstValueFrom(this.lectureService.unlockNextQuestion(lectureId));
    this.status.set('Unlocked next question');
    await this.refreshLectureState();
  }

  async createInvite() {
    const lectureId = this.createdLectureId();
    if (!lectureId) {
      return;
    }

    const invite = await firstValueFrom(this.lectureService.createInvite(lectureId));
    this.invite.set(invite);
    this.status.set(`Invite generated (code: ${invite.joinCode})`);
  }

  async joinLecture() {
    if (this.joinLectureForm.invalid) {
      return;
    }

    const code = this.joinLectureForm.value.code!.trim().toUpperCase();
    const result = await firstValueFrom(this.lectureService.joinLecture({ code }));
    this.createdLectureId.set(result.lectureId);
    this.joinResult.set(result.alreadyEnrolled ? 'Already enrolled' : 'Enrolled successfully');
  }

  async loadNextQuestion() {
    const lectureId = this.createdLectureId();
    if (!lectureId) {
      return;
    }

    const nextQuestion = await firstValueFrom(this.lectureService.getNextQuestion(lectureId));
    this.nextQuestion.set(nextQuestion);
    this.cooldownMessage.set('');
  }

  async submitAnswer() {
    const lectureId = this.createdLectureId();
    const currentQuestion = this.nextQuestion();
    if (!lectureId || !currentQuestion || !currentQuestion.hasQuestion || this.submitAnswerForm.invalid) {
      return;
    }

    this.cooldownMessage.set('');

    try {
      await firstValueFrom(
        this.lectureService.submitAnswer(lectureId, {
          questionId: currentQuestion.questionId,
          answerText: this.submitAnswerForm.value.answerText!,
        }),
      );
      this.submitAnswerForm.reset({ answerText: '' });
      await this.loadNextQuestion();
    } catch (error: any) {
      if (error?.status === 429) {
        const retryAfter = error?.error?.details?.retryAfterSeconds;
        this.cooldownMessage.set(
          retryAfter
            ? `Cooldown active: retry in ${retryAfter}s`
            : 'Cooldown active. Please retry later.',
        );
        return;
      }
      throw error;
    }
  }

  private async refreshLectureState() {
    const lectureId = this.createdLectureId();
    if (!lectureId) {
      return;
    }
    const state = await firstValueFrom(this.lectureService.getState(lectureId));
    this.lectureState.set(state);
  }

}
