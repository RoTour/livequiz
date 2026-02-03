import { Component, inject } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CreateQuizUsecaseService } from '../../application/CreateQuiz.usecase.service';

@Component({
  selector: 'app-create-quiz',
  templateUrl: './create-quiz.component.html',
  imports: [ReactiveFormsModule],
  styleUrls: ['./create-quiz.component.css'],
})
export class CreateQuizComponent {
  private quizService = inject(CreateQuizUsecaseService);

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
      const { id } = await this.quizService.execute(dto.title);
      console.log('Quiz created with ID:', id);
      this.form.enable();
      this.form.reset();
    } catch (e) {
      console.error('Error creating quiz:', e);
      this.form.enable();
    }
  }
}
