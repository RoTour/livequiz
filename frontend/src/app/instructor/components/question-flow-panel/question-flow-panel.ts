import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-question-flow-panel',
  imports: [ReactiveFormsModule],
  templateUrl: './question-flow-panel.html',
})
export class QuestionFlowPanel {
  @Input({ required: true }) form!: FormGroup;
  @Input({ required: true }) selectedLectureId!: string;
  @Output() addQuestion = new EventEmitter<void>();
  @Output() unlockNext = new EventEmitter<void>();
  @Output() refreshState = new EventEmitter<void>();

  protected submit() {
    this.addQuestion.emit();
  }

  protected unlockNextQuestion() {
    this.unlockNext.emit();
  }

  protected refreshLectureState() {
    this.refreshState.emit();
  }
}
