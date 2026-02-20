import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NextQuestionResponse } from '../../../lecture.service';

@Component({
  selector: 'app-answer-flow-panel',
  imports: [ReactiveFormsModule],
  templateUrl: './answer-flow-panel.html',
})
export class AnswerFlowPanel {
  @Input({ required: true }) selectedLectureId!: string;
  @Input({ required: true }) nextQuestion!: NextQuestionResponse | null;
  @Input({ required: true }) submitAnswerForm!: FormGroup;
  @Input({ required: true }) cooldownMessage!: string;
  @Output() loadNextQuestion = new EventEmitter<void>();
  @Output() submitAnswer = new EventEmitter<void>();

  protected loadNext() {
    this.loadNextQuestion.emit();
  }

  protected submit() {
    this.submitAnswer.emit();
  }
}
