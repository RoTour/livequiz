import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NextQuestionResponse } from '../lecture.service';
import { StudentWorkspaceService } from './application/student-workspace.service';
import { AnswerFlowPanel } from './components/answer-flow-panel/answer-flow-panel';

@Component({
  selector: 'app-student-lecture-room',
  imports: [ReactiveFormsModule, AnswerFlowPanel],
  templateUrl: './student-lecture-room.html',
})
export class StudentLectureRoom implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);
  private readonly workspaceService = inject(StudentWorkspaceService);

  protected readonly status = signal('Ready');
  protected readonly selectedLectureId = signal('');
  protected readonly nextQuestion = signal<NextQuestionResponse | null>(null);
  protected readonly cooldownMessage = signal('');

  readonly submitAnswerForm = new FormGroup({
    answerText: new FormControl('', [Validators.required, Validators.minLength(2)]),
  });

  async ngOnInit() {
    await this.loadLectureContext(this.route.snapshot.paramMap.get('lectureId'));
    this.route.paramMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((paramMap) => {
      void this.loadLectureContext(paramMap.get('lectureId'));
    });
  }

  async loadNextQuestion() {
    const lectureId = this.selectedLectureId();
    if (!lectureId) {
      return;
    }

    try {
      const nextQuestion = await this.workspaceService.getNextQuestion(lectureId);
      this.nextQuestion.set(nextQuestion);
      this.cooldownMessage.set('');
      this.status.set(nextQuestion.hasQuestion ? 'Question loaded' : 'No unlocked pending question');
    } catch (error: any) {
      const errorCode = error?.error?.code;
      if (errorCode === 'LECTURE_ENROLLMENT_REQUIRED') {
        this.status.set('Enrollment required before loading questions.');
        return;
      }
      this.status.set('Could not load next question. Please retry.');
    }
  }

  async submitAnswer() {
    const lectureId = this.selectedLectureId();
    const currentQuestion = this.nextQuestion();
    if (!lectureId || !currentQuestion || !currentQuestion.hasQuestion || this.submitAnswerForm.invalid) {
      return;
    }

    this.cooldownMessage.set('');

    try {
      await this.workspaceService.submitAnswer(lectureId, {
        questionId: currentQuestion.questionId,
        answerText: this.submitAnswerForm.value.answerText!,
      });
      this.submitAnswerForm.reset({ answerText: '' });
      this.status.set('Answer submitted');
      await this.loadNextQuestion();
    } catch (error: any) {
      if (error?.status === 429) {
        const retryAfter = error?.error?.details?.retryAfterSeconds;
        this.cooldownMessage.set(
          retryAfter ? `Cooldown active: retry in ${retryAfter}s` : 'Cooldown active. Please retry later.',
        );
        return;
      }
      this.status.set('Could not submit answer. Please retry.');
    }
  }

  private async loadLectureContext(lectureId: string | null) {
    const normalizedLectureId = lectureId?.trim() ?? '';
    if (!normalizedLectureId) {
      this.selectedLectureId.set('');
      this.nextQuestion.set(null);
      this.submitAnswerForm.reset({ answerText: '' });
      this.cooldownMessage.set('');
      this.status.set('Lecture not selected. Return to your lecture list.');
      return;
    }

    this.selectedLectureId.set(normalizedLectureId);
    this.nextQuestion.set(null);
    this.submitAnswerForm.reset({ answerText: '' });
    this.cooldownMessage.set('');
    await this.loadNextQuestion();
  }
}
