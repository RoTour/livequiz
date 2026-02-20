import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-create-lecture-panel',
  imports: [ReactiveFormsModule],
  templateUrl: './create-lecture-panel.html',
})
export class CreateLecturePanel {
  @Input({ required: true }) form!: FormGroup;
  @Input({ required: true }) selectedLectureId!: string;
  @Output() createLecture = new EventEmitter<void>();

  protected submit() {
    this.createLecture.emit();
  }
}
