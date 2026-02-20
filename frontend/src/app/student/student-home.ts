import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { NextQuestionResponse } from '../lecture.service';
import { StudentWorkspaceService } from './application/student-workspace.service';
import { JoinLecturePanel } from './components/join-lecture-panel/join-lecture-panel';
import { AnswerFlowPanel } from './components/answer-flow-panel/answer-flow-panel';

@Component({
  selector: 'app-student-home',
  imports: [ReactiveFormsModule, JoinLecturePanel, AnswerFlowPanel],
  templateUrl: './student-home.html',
  styleUrl: './student-home.css',
})
export class StudentHome implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly workspaceService = inject(StudentWorkspaceService);

  protected readonly status = signal('Ready');
  protected readonly selectedLectureId = signal('');
  protected readonly joinResult = signal('');
  protected readonly nextQuestion = signal<NextQuestionResponse | null>(null);
  protected readonly cooldownMessage = signal('');

  readonly joinLectureForm = new FormGroup({
    code: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]),
  });

  readonly submitAnswerForm = new FormGroup({
    answerText: new FormControl('', [Validators.required, Validators.minLength(2)]),
  });

  ngOnInit() {
    const lectureIdFromLink = this.route.snapshot.queryParamMap.get('lectureId')?.trim();
    if (!lectureIdFromLink) {
      return;
    }

    this.selectedLectureId.set(lectureIdFromLink);
    this.nextQuestion.set(null);
    this.submitAnswerForm.reset({ answerText: '' });
    this.cooldownMessage.set('');

    const alreadyEnrolled = this.route.snapshot.queryParamMap.get('alreadyEnrolled') === '1';
    this.joinResult.set(alreadyEnrolled ? 'Already enrolled' : 'Enrolled successfully');
    this.status.set('Lecture joined from invite link');
  }

  async joinLecture() {
    if (this.joinLectureForm.invalid) {
      return;
    }

    try {
      const code = this.joinLectureForm.value.code!.trim().toUpperCase();
      const result = await this.workspaceService.joinLectureByCode(code);
      this.nextQuestion.set(null);
      this.submitAnswerForm.reset({ answerText: '' });
      this.cooldownMessage.set('');
      this.selectedLectureId.set(result.lectureId);
      this.joinResult.set(result.alreadyEnrolled ? 'Already enrolled' : 'Enrolled successfully');
      this.status.set('Lecture joined');
    } catch {
      this.status.set('Could not join lecture. Verify code and retry.');
    }
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
          retryAfter
            ? `Cooldown active: retry in ${retryAfter}s`
            : 'Cooldown active. Please retry later.',
        );
        return;
      }
      this.status.set('Could not submit answer. Please retry.');
    }
  }
}
