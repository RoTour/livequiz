import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { JoinLectureResponse, LectureService, NextQuestionResponse, SubmitAnswerResponse } from '../../lecture.service';

@Injectable({ providedIn: 'root' })
export class StudentWorkspaceService {
  private readonly lectureService = inject(LectureService);

  async joinLectureByCode(code: string): Promise<JoinLectureResponse> {
    return await firstValueFrom(this.lectureService.joinLecture({ code }));
  }

  async getNextQuestion(lectureId: string): Promise<NextQuestionResponse> {
    return await firstValueFrom(this.lectureService.getNextQuestion(lectureId));
  }

  async submitAnswer(
    lectureId: string,
    payload: { questionId: string; answerText: string },
  ): Promise<SubmitAnswerResponse> {
    return await firstValueFrom(this.lectureService.submitAnswer(lectureId, payload));
  }
}
