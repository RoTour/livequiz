import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  CreateInviteResponse,
  InstructorLectureSummaryResponse,
  LectureInviteResponse,
  LectureService,
  LectureStateResponse,
  QuestionAnalyticsResponse,
  ReviewStatus,
  StudentSubmissionReviewsResponse,
  SubmissionReviewCommandResponse,
  StudentAnswerHistoryResponse,
} from '../../lecture.service';

@Injectable({ providedIn: 'root' })
export class InstructorWorkspaceService {
  private readonly lectureService = inject(LectureService);

  async createLecture(title: string): Promise<string> {
    const response = await firstValueFrom(this.lectureService.create({ title }));
    return response.lectureId;
  }

  async listLectures(): Promise<InstructorLectureSummaryResponse[]> {
    return await firstValueFrom(this.lectureService.listInstructorLectures());
  }

  async addQuestion(
    lectureId: string,
    payload: { prompt: string; modelAnswer: string; timeLimitSeconds: number },
  ): Promise<void> {
    await firstValueFrom(this.lectureService.addQuestion(lectureId, payload));
  }

  async unlockNextQuestion(lectureId: string): Promise<void> {
    await firstValueFrom(this.lectureService.unlockNextQuestion(lectureId));
  }

  async unlockQuestion(lectureId: string, questionId: string): Promise<void> {
    await firstValueFrom(this.lectureService.unlockQuestion(lectureId, questionId));
  }

  async getLectureState(lectureId: string): Promise<LectureStateResponse> {
    return await firstValueFrom(this.lectureService.getState(lectureId));
  }

  async listQuestionAnalytics(lectureId: string): Promise<QuestionAnalyticsResponse[]> {
    return await firstValueFrom(this.lectureService.getQuestionAnalytics(lectureId));
  }

  async listQuestionAnswerHistory(
    lectureId: string,
    questionId: string,
  ): Promise<StudentAnswerHistoryResponse[]> {
    return await firstValueFrom(this.lectureService.getQuestionAnswerHistory(lectureId, questionId));
  }

  async listQuestionSubmissionReviews(
    lectureId: string,
    questionId: string,
  ): Promise<StudentSubmissionReviewsResponse[]> {
    return await firstValueFrom(this.lectureService.getQuestionSubmissionReviews(lectureId, questionId));
  }

  async upsertSubmissionReview(
    lectureId: string,
    questionId: string,
    submissionId: string,
    payload: {
      reviewStatus: Exclude<ReviewStatus, 'AWAITING_REVIEW'>;
      reviewComment: string;
      published: boolean;
    },
  ): Promise<SubmissionReviewCommandResponse> {
    return await firstValueFrom(
      this.lectureService.upsertSubmissionReview(lectureId, questionId, submissionId, payload),
    );
  }

  async acceptSubmissionLlmReview(
    lectureId: string,
    questionId: string,
    submissionId: string,
    payload: { published: boolean },
  ): Promise<SubmissionReviewCommandResponse> {
    return await firstValueFrom(
      this.lectureService.acceptSubmissionLlmReview(lectureId, questionId, submissionId, payload),
    );
  }

  async createInvite(lectureId: string): Promise<CreateInviteResponse> {
    return await firstValueFrom(this.lectureService.createInvite(lectureId));
  }

  async listInvites(lectureId: string): Promise<LectureInviteResponse[]> {
    return await firstValueFrom(this.lectureService.listInvites(lectureId));
  }

  async revokeInvite(lectureId: string, inviteId: string): Promise<void> {
    await firstValueFrom(this.lectureService.revokeInvite(lectureId, inviteId));
  }
}
