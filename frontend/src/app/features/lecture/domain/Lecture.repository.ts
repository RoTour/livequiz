import { Lecture } from './Lecture.entity';

export abstract class LectureRepository {
  abstract create(lecture: Partial<Lecture>): Promise<void>;
}
