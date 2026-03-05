import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  LectureStateResponse,
  QuestionAnalyticsResponse,
  StudentAnswerHistoryResponse,
} from '../../../lecture.service';
import { HumanDatePipe } from '../../../shared/date/human-date.pipe';

@Component({
  selector: 'app-lecture-state-panel',
  imports: [HumanDatePipe],
  templateUrl: './lecture-state-panel.html',
  styleUrl: './lecture-state-panel.css',
})
export class LectureStatePanel {
  @Input({ required: true }) lectureState!: LectureStateResponse | null;
  @Input({ required: true }) questionAnalytics!: QuestionAnalyticsResponse[];
  @Input({ required: true }) analyticsLoading!: boolean;
  @Input({ required: true }) analyticsError!: string;
  @Input({ required: true }) selectedHistoryQuestionId!: string;
  @Input({ required: true }) questionHistory!: StudentAnswerHistoryResponse[];
  @Input({ required: true }) questionHistoryLoading!: boolean;
  @Input({ required: true }) questionHistoryError!: string;

  @Output() unlockQuestion = new EventEmitter<string>();
  @Output() refreshAnalytics = new EventEmitter<void>();
  @Output() openHistory = new EventEmitter<string>();
  @Output() closeHistory = new EventEmitter<void>();

  protected unlock(questionId: string) {
    this.unlockQuestion.emit(questionId);
  }

  protected refreshQuestionAnalytics() {
    this.refreshAnalytics.emit();
  }

  protected viewHistory(questionId: string) {
    this.openHistory.emit(questionId);
  }

  protected dismissHistory() {
    this.closeHistory.emit();
  }

  protected analyticsByQuestionId(questionId: string): QuestionAnalyticsResponse | null {
    return this.questionAnalytics.find((analytics) => analytics.questionId === questionId) ?? null;
  }

  protected isHistoryOpen(questionId: string): boolean {
    return this.selectedHistoryQuestionId === questionId;
  }

  protected studentDisplayLabel(row: StudentAnswerHistoryResponse): string {
    return row.studentEmail ?? row.studentId;
  }

  protected hasVerifiedEmail(row: StudentAnswerHistoryResponse): boolean {
    return row.studentEmail !== null;
  }
}
