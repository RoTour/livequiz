import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' }) // Singleton, available application-wide
export class QuizService {
  private http = inject(HttpClient);
  private endpoint = '/api/quizzes';

  create(dto: CreateQuizDto): Observable<{ id: string }> {
    return this.http.post<{ id: string }>(this.endpoint, dto);
  }
}

type CreateQuizDto = {
  title: string;
};
