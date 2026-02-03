import { inject, Injectable } from '@angular/core';
import { QuizRepository } from '../domain/Quiz.repository';
import { v7 } from 'uuid';
import { Quiz } from '../domain/Quiz.entity';

@Injectable({providedIn: 'root'})
export class CreateQuizUsecaseService {
  quizRepository: QuizRepository = inject(QuizRepository);

  async execute(title: string): Promise<Quiz> {
    const uuid = v7();
    const quiz = {
      id: uuid,
      title,
    };
    await this.quizRepository.create(quiz);

    return quiz;
  }
}
