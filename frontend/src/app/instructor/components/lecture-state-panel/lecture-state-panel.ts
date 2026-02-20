import { Component, EventEmitter, Input, Output } from '@angular/core';
import { LectureStateResponse } from '../../../lecture.service';

@Component({
  selector: 'app-lecture-state-panel',
  imports: [],
  templateUrl: './lecture-state-panel.html',
})
export class LectureStatePanel {
  @Input({ required: true }) lectureState!: LectureStateResponse | null;
  @Output() unlockQuestion = new EventEmitter<string>();

  protected unlock(questionId: string) {
    this.unlockQuestion.emit(questionId);
  }
}
