import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-join-lecture-panel',
  imports: [ReactiveFormsModule],
  templateUrl: './join-lecture-panel.html',
})
export class JoinLecturePanel {
  @Input({ required: true }) joinLectureForm!: FormGroup;
  @Input({ required: true }) selectedLectureId!: string;
  @Input({ required: true }) joinResult!: string;
  @Output() joinLecture = new EventEmitter<void>();

  protected submit() {
    this.joinLecture.emit();
  }
}
