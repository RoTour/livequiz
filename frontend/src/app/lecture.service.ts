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

  addQuestion(lectureId: string, dto: AddQuestionDto): Observable<{ lectureId: string; questionId: string }> {
    return this.http.post<{ lectureId: string; questionId: string }>(
      `${this.endpoint}/${lectureId}/questions`,
      dto,
    );
  }

  unlockNextQuestion(lectureId: string): Observable<{ lectureId: string }> {
    return this.http.post<{ lectureId: string }>(`${this.endpoint}/${lectureId}/questions/unlock-next`, {});
  }

  getState(lectureId: string): Observable<LectureStateResponse> {
    return this.http.get<LectureStateResponse>(`${this.endpoint}/${lectureId}/state`);
  }

  createInvite(lectureId: string): Observable<CreateInviteResponse> {
    return this.http.post<CreateInviteResponse>(`${this.endpoint}/${lectureId}/invites`, {});
  }

  joinLecture(dto: JoinLectureDto): Observable<JoinLectureResponse> {
    return this.http.post<JoinLectureResponse>(`${this.endpoint}/join`, dto);
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

export type CreateInviteResponse = {
  inviteId: string;
  lectureId: string;
  joinCode: string;
  joinUrl: string;
  expiresAt: string;
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
