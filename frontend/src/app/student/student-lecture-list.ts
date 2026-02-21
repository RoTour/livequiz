import { Component, inject, OnInit, signal } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { StudentLectureSummaryResponse } from '../lecture.service';
import { StudentWorkspaceService } from './application/student-workspace.service';

@Component({
  selector: 'app-student-lecture-list',
  imports: [ReactiveFormsModule],
  templateUrl: './student-lecture-list.html',
})
export class StudentLectureList implements OnInit {
  private readonly workspaceService = inject(StudentWorkspaceService);
  private readonly router = inject(Router);

  protected readonly status = signal('Ready');
  protected readonly loading = signal(false);
  protected readonly lectures = signal<StudentLectureSummaryResponse[]>([]);
  protected readonly joinResult = signal('');

  readonly joinLectureForm = new FormGroup({
    code: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]),
  });

  ngOnInit() {
    void this.refreshLectures();
  }

  async refreshLectures() {
    this.loading.set(true);
    try {
      const lectures = await this.workspaceService.listLectures();
      this.lectures.set(lectures);
      this.status.set('Lectures loaded');
    } catch {
      this.status.set('Could not load your lectures. Please retry.');
    } finally {
      this.loading.set(false);
    }
  }

  async joinLecture() {
    if (this.joinLectureForm.invalid) {
      return;
    }

    try {
      const code = this.joinLectureForm.value.code!.trim().toUpperCase();
      const result = await this.workspaceService.joinLectureByCode(code);
      this.joinResult.set(result.alreadyEnrolled ? 'Already enrolled' : 'Enrolled successfully');
      this.joinLectureForm.reset({ code: '' });
      await this.refreshLectures();
      await this.router.navigate(['/student/lectures', result.lectureId]);
    } catch {
      this.status.set('Could not join lecture. Verify code and retry.');
    }
  }

  async openLecture(lectureId: string) {
    await this.router.navigate(['/student/lectures', lectureId]);
  }
}
