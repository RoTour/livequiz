import { LectureId } from './LectureId.vo';

type LectureProps = {
  id: LectureId;
  title: string;
}

export class Lecture {
  id: LectureId;
  title: string;

  constructor({ id, title }: LectureProps) {
    this.id = id;
    this.title = title;
  }
}
