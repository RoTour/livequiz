import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NextQuestionResponse, StudentAnswerStatusResponse } from '../../../lecture.service';
import { HumanDatePipe } from '../../../shared/date/human-date.pipe';

@Component({
  selector: 'app-answer-flow-panel',
  imports: [ReactiveFormsModule, HumanDatePipe],
  templateUrl: './answer-flow-panel.html',
})
export class AnswerFlowPanel {
  @Input({ required: true }) selectedLectureId!: string;
  @Input({ required: true }) nextQuestion!: NextQuestionResponse | null;
  @Input({ required: true }) answerStatuses!: StudentAnswerStatusResponse[];
  @Input({ required: true }) submitAnswerForm!: FormGroup;
  @Input({ required: true }) cooldownMessage!: string;
  @Input({ required: true }) manualReloadDisabled!: boolean;
  @Output() loadNextQuestion = new EventEmitter<void>();
  @Output() submitAnswer = new EventEmitter<void>();

  protected loadNext() {
    this.loadNextQuestion.emit();
  }

  protected submit() {
    this.submitAnswer.emit();
  }

  protected answerStatusLabel(status: string): string {
    return status.toLowerCase().replaceAll('_', ' ');
  }
}
