import { Component, inject } from '@angular/core';
import { QuizService } from './quiz.service';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-create-quiz',
  templateUrl: './create-quiz.component.html',
  imports: [ReactiveFormsModule],
  styleUrls: ['./create-quiz.component.css'],
})
export class CreateQuizComponent {
  private quizService = inject(QuizService);

  form = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  submit() {
    if (this.form.invalid) return;

    this.form.disable();

    const dto = {
      title: this.form.value.title!,
    };

    this.quizService.create(dto).subscribe({
      next: (response) => {
        console.log('Quiz created with ID:', response.id);
        this.form.enable();
        this.form.reset();
      },
      error: (error) => {
        console.error('Error creating quiz:', error);
        this.form.enable();
      },
    });
  }
}
