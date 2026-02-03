import { QuizRepository } from '../domain/Quiz.repository';
import { Quiz } from '../domain/Quiz.entity';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { inject } from '@angular/core';

export class QuizHttpRepository implements QuizRepository {
  httpClient = inject(HttpClient);

  async create(quiz: Partial<Quiz>): Promise<void> {
    await firstValueFrom(this.httpClient.post('/api/quizzes', quiz));
  }
}
