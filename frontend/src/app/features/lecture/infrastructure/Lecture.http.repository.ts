import { HttpClient } from '@angular/common/http';
import { inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { Lecture } from '../domain/Lecture.entity';
import { LectureRepository } from '../domain/Lecture.repository';

export class LectureHttpRepository implements LectureRepository {
  httpClient = inject(HttpClient);

  async create(lecture: Partial<Lecture>): Promise<void> {
    await firstValueFrom(this.httpClient.post('/api/lectures', lecture));
  }
}
