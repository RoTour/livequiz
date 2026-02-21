import { inject, Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import {
  JoinLectureResponse,
  LectureService,
  NextQuestionResponse,
  StudentLectureSummaryResponse,
  SubmitAnswerResponse,
} from '../../lecture.service';

@Injectable({ providedIn: 'root' })
export class StudentWorkspaceService {
  private readonly lectureService = inject(LectureService);

  async joinLectureByCode(code: string): Promise<JoinLectureResponse> {
    return await firstValueFrom(this.lectureService.joinLecture({ code }));
  }

  async joinLectureByToken(token: string): Promise<JoinLectureResponse> {
    return await firstValueFrom(this.lectureService.joinLecture({ token }));
  }

  async listLectures(): Promise<StudentLectureSummaryResponse[]> {
    return await firstValueFrom(this.lectureService.listStudentLectures());
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
