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
  latestAnswerAt: string | null;
  attemptCount: number;
  latestAnswerText: string | null;
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
};
