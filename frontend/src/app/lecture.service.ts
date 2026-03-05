import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' }) // Singleton, available application-wide
export class LectureService {
  private http = inject(HttpClient);
  private endpoint = '/api/lectures';

  create(dto: CreateLectureDto): Observable<{ lectureId: string }> {
    return this.http.post<{ lectureId: string }>(this.endpoint, dto);
  }

  listInstructorLectures(): Observable<InstructorLectureSummaryResponse[]> {
    return this.http.get<InstructorLectureSummaryResponse[]>(this.endpoint);
  }

  addQuestion(lectureId: string, dto: AddQuestionDto): Observable<{ lectureId: string; questionId: string }> {
    return this.http.post<{ lectureId: string; questionId: string }>(
      `${this.endpoint}/${lectureId}/questions`,
      dto,
    );
  }

  unlockNextQuestion(lectureId: string): Observable<{ lectureId: string }> {
    return this.http.post<{ lectureId: string }>(`${this.endpoint}/${lectureId}/questions/unlock-next`, {});
  }

  unlockQuestion(lectureId: string, questionId: string): Observable<{ lectureId: string; questionId: string }> {
    return this.http.post<{ lectureId: string; questionId: string }>(
      `${this.endpoint}/${lectureId}/questions/${questionId}/unlock`,
      {},
    );
  }

  getState(lectureId: string): Observable<LectureStateResponse> {
    return this.http.get<LectureStateResponse>(`${this.endpoint}/${lectureId}/state`);
  }

  getQuestionAnalytics(lectureId: string): Observable<QuestionAnalyticsResponse[]> {
    return this.http.get<QuestionAnalyticsResponse[]>(`${this.endpoint}/${lectureId}/questions/analytics`);
  }

  getQuestionAnswerHistory(
    lectureId: string,
    questionId: string,
  ): Observable<StudentAnswerHistoryResponse[]> {
    return this.http.get<StudentAnswerHistoryResponse[]>(
      `${this.endpoint}/${lectureId}/questions/${questionId}/answers/history`,
    );
  }

  getQuestionSubmissionReviews(
    lectureId: string,
    questionId: string,
  ): Observable<StudentSubmissionReviewsResponse[]> {
    return this.http.get<StudentSubmissionReviewsResponse[]>(
      `${this.endpoint}/${lectureId}/questions/${questionId}/answers/reviews`,
    );
  }

  upsertSubmissionReview(
    lectureId: string,
    questionId: string,
    submissionId: string,
    dto: UpsertSubmissionReviewDto,
  ): Observable<SubmissionReviewCommandResponse> {
    return this.http.put<SubmissionReviewCommandResponse>(
      `${this.endpoint}/${lectureId}/questions/${questionId}/answers/${submissionId}/review`,
      dto,
    );
  }

  acceptSubmissionLlmReview(
    lectureId: string,
    questionId: string,
    submissionId: string,
    dto: AcceptSubmissionLlmReviewDto,
  ): Observable<SubmissionReviewCommandResponse> {
    return this.http.post<SubmissionReviewCommandResponse>(
      `${this.endpoint}/${lectureId}/questions/${questionId}/answers/${submissionId}/llm-review/accept`,
      dto,
    );
  }

  createInvite(lectureId: string): Observable<CreateInviteResponse> {
    return this.http.post<CreateInviteResponse>(`${this.endpoint}/${lectureId}/invites`, {});
  }

  listInvites(lectureId: string): Observable<LectureInviteResponse[]> {
    return this.http.get<LectureInviteResponse[]>(`${this.endpoint}/${lectureId}/invites`);
  }

  revokeInvite(lectureId: string, inviteId: string): Observable<LectureInviteResponse> {
    return this.http.post<LectureInviteResponse>(`${this.endpoint}/${lectureId}/invites/${inviteId}/revoke`, {});
  }

  joinLecture(dto: JoinLectureDto): Observable<JoinLectureResponse> {
    return this.http.post<JoinLectureResponse>(`${this.endpoint}/join`, dto);
  }

  listStudentLectures(): Observable<StudentLectureSummaryResponse[]> {
    return this.http.get<StudentLectureSummaryResponse[]>(`${this.endpoint}/students/me`);
  }

  getNextQuestion(lectureId: string): Observable<NextQuestionResponse> {
    return this.http.get<NextQuestionResponse>(`${this.endpoint}/${lectureId}/students/me/next-question`);
  }

  getAnswerStatuses(lectureId: string): Observable<StudentAnswerStatusResponse[]> {
    return this.http.get<StudentAnswerStatusResponse[]>(
      `${this.endpoint}/${lectureId}/students/me/answer-statuses`,
    );
  }

  submitAnswer(lectureId: string, dto: SubmitAnswerDto): Observable<SubmitAnswerResponse> {
    return this.http.post<SubmitAnswerResponse>(`${this.endpoint}/${lectureId}/submissions`, dto);
  }
}

type CreateLectureDto = {
  title: string;
};

type AddQuestionDto = {
  prompt: string;
  modelAnswer: string;
  timeLimitSeconds: number;
};

export type LectureStateResponse = {
  lectureId: string;
  title: string;
  questions: Array<{
    questionId: string;
    prompt: string;
    order: number;
    timeLimitSeconds: number;
    unlocked: boolean;
  }>;
};

export type InstructorLectureSummaryResponse = {
  lectureId: string;
  title: string;
  createdAt: string | null;
  questionCount: number;
  unlockedCount: number;
};

export type QuestionAnalyticsResponse = {
  questionId: string;
  prompt: string;
  order: number;
  enrolledCount: number;
  answeredCount: number;
  unansweredCount: number;
  multiAttemptCount: number;
};

export type StudentAnswerHistoryResponse = {
  studentId: string;
  studentEmail: string | null;
  latestAnswerAt: string | null;
  attemptCount: number;
  latestAnswerText: string | null;
};

export type ReviewStatus = 'AWAITING_REVIEW' | 'CORRECT' | 'INCOMPLETE' | 'NEEDS_IMPROVEMENT';

export type SubmissionAttemptReviewResponse = {
  submissionId: string;
  answeredAt: string;
  answerText: string;
  reviewStatus: ReviewStatus;
  reviewPublished: boolean;
  reviewComment: string | null;
  reviewUpdatedAt: string | null;
  reviewCreatedAt: string | null;
  reviewPublishedAt: string | null;
  reviewedByInstructorId: string | null;
  reviewOrigin: string | null;
  llmSuggestedStatus: Exclude<ReviewStatus, 'AWAITING_REVIEW'> | null;
  llmSuggestedComment: string | null;
  llmSuggestedAt: string | null;
  llmSuggestedModel: string | null;
  llmAcceptedAt: string | null;
  llmAcceptedByInstructorId: string | null;
};

export type StudentSubmissionReviewsResponse = {
  studentId: string;
  studentEmail: string | null;
  attempts: SubmissionAttemptReviewResponse[];
};

type UpsertSubmissionReviewDto = {
  reviewStatus: Exclude<ReviewStatus, 'AWAITING_REVIEW'>;
  reviewComment: string;
  published: boolean;
};

type AcceptSubmissionLlmReviewDto = {
  published: boolean;
};

export type SubmissionReviewCommandResponse = {
  submissionId: string;
  reviewStatus: Exclude<ReviewStatus, 'AWAITING_REVIEW'>;
  reviewPublished: boolean;
  reviewUpdatedAt: string | null;
  reviewedByInstructorId: string | null;
  llmAcceptedAt: string | null;
};

export type CreateInviteResponse = {
  inviteId: string;
  lectureId: string;
  joinCode: string;
  joinUrl: string;
  expiresAt: string;
  active: boolean;
};

export type LectureInviteResponse = {
  inviteId: string;
  lectureId: string;
  joinCode: string;
  createdAt: string;
  expiresAt: string;
  revokedAt: string | null;
  active: boolean;
};

type JoinLectureDto = {
  token?: string;
  code?: string;
};

export type JoinLectureResponse = {
  lectureId: string;
  studentId: string;
  alreadyEnrolled: boolean;
  enrolledAt: string;
};

export type StudentLectureSummaryResponse = {
  lectureId: string;
  title: string;
  enrolledAt: string;
  questionCount: number;
  answeredCount: number;
};

export type NextQuestionResponse =
  | {
      hasQuestion: false;
    }
  | {
      hasQuestion: true;
      lectureId: string;
      questionId: string;
      prompt: string;
      order: number;
      timeLimitSeconds: number;
    };

type SubmitAnswerDto = {
  questionId: string;
  answerText: string;
};

export type SubmitAnswerResponse = {
  submissionId: string;
  lectureId: string;
  questionId: string;
  studentId: string;
  answerStatus: StudentAnswerStatus;
};

export type StudentAnswerStatus = ReviewStatus;

export type StudentAnswerStatusResponse = {
  lectureId: string;
  questionId: string;
  prompt: string;
  order: number;
  status: StudentAnswerStatus;
  submittedAt: string;
};
