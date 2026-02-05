import { inject, Injectable } from '@angular/core';
import { v7 } from 'uuid';
import { Lecture } from '../domain/Lecture.entity';
import { LectureRepository } from '../domain/Lecture.repository';

@Injectable({providedIn: 'root'})
export class CreateLectureUsecaseService {
  lectureRepository: LectureRepository = inject(LectureRepository);

  async execute(title: string): Promise<Lecture> {
    const uuid = v7();
    const lecture = {
      id: uuid,
      title,
    };
    await this.lectureRepository.create(lecture);

    return lecture;
  }
}
