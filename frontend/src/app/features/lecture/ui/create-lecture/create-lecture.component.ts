import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateLectureUsecaseService } from '../../application/CreateLecture.usecase.service';

@Component({
  selector: 'app-create-lecture',
  templateUrl: './create-lecture.component.html',
  imports: [ReactiveFormsModule],
  styleUrls: ['./create-lecture.component.css'],
})
export class CreateLectureComponent {
  private lectureService = inject(CreateLectureUsecaseService);

  form = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  async submit() {
    if (this.form.invalid) return;

    this.form.disable();

    const dto = {
      title: this.form.value.title!,
    };

    try {
      const { id } = await this.lectureService.execute(dto.title);
      console.log('Lecture created with ID:', id);
      this.form.enable();
      this.form.reset();
    } catch (e) {
      console.error('Error creating lecture:', e);
      this.form.enable();
    }
  }
}
