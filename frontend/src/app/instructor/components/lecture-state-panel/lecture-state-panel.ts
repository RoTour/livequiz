import { Component, EventEmitter, Input, Output } from '@angular/core';
import {
  LectureStateResponse,
  QuestionAnalyticsResponse,
  ReviewStatus,
  StudentSubmissionReviewsResponse,
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
  @Input({ required: true }) questionReviews!: StudentSubmissionReviewsResponse[];
  @Input({ required: true }) questionHistoryLoading!: boolean;
  @Input({ required: true }) questionHistoryError!: string;

  @Output() unlockQuestion = new EventEmitter<string>();
  @Output() refreshAnalytics = new EventEmitter<void>();
  @Output() openHistory = new EventEmitter<string>();
  @Output() closeHistory = new EventEmitter<void>();
  @Output() saveReview = new EventEmitter<{
    submissionId: string;
    reviewStatus: Exclude<ReviewStatus, 'AWAITING_REVIEW'>;
    reviewComment: string;
    published: boolean;
  }>();
  @Output() acceptLlmReview = new EventEmitter<{
    submissionId: string;
    published: boolean;
  }>();

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

  protected studentDisplayLabel(row: StudentSubmissionReviewsResponse): string {
    return row.studentEmail ?? row.studentId;
  }

  protected hasVerifiedEmail(row: StudentSubmissionReviewsResponse): boolean {
    return row.studentEmail !== null;
  }

  protected hasLlmSuggestion(llmSuggestedStatus: string | null): boolean {
    return llmSuggestedStatus !== null && llmSuggestedStatus.length > 0;
  }

  protected awaitingReviewCount(): number {
    return this.questionReviews
      .flatMap((row) => row.attempts)
      .filter((attempt) => attempt.reviewStatus === 'AWAITING_REVIEW').length;
  }

  protected saveReviewForAttempt(
    submissionId: string,
    reviewStatus: string,
    reviewComment: string,
    published: boolean,
  ) {
    this.saveReview.emit({
      submissionId,
      reviewStatus: this.normalizeReviewStatus(reviewStatus),
      reviewComment,
      published,
    });
  }

  protected acceptLlmForAttempt(submissionId: string, published: boolean) {
    this.acceptLlmReview.emit({ submissionId, published });
  }

  protected reviewStatusLabel(status: string): string {
    return status.toLowerCase().replaceAll('_', ' ');
  }

  private normalizeReviewStatus(status: string): Exclude<ReviewStatus, 'AWAITING_REVIEW'> {
    if (status === 'CORRECT' || status === 'INCOMPLETE' || status === 'NEEDS_IMPROVEMENT') {
      return status;
    }
    return 'NEEDS_IMPROVEMENT';
  }
}
