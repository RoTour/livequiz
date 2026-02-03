import { QuizId } from './QuizId.vo';

type QuizProps = {
  id: QuizId;
  title: string;
}

export class Quiz {
  id: QuizId;
  title: string;

  constructor({ id, title }: QuizProps) {
    this.id = id;
    this.title = title;
  }
}
