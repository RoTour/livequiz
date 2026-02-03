import { Quiz } from './Quiz.entity';

export abstract class QuizRepository {
  abstract create(quiz: Partial<Quiz>): Promise<void>;
}
