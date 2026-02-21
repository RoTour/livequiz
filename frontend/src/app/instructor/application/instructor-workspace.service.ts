import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  CreateInviteResponse,
  InstructorLectureSummaryResponse,
  LectureInviteResponse,
  LectureService,
  LectureStateResponse,
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
