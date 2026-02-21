import { Component, inject, OnInit, signal } from '@angular/core';
import { Router } from '@angular/router';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { InstructorLectureSummaryResponse } from '../lecture.service';
import { InstructorWorkspaceService } from './application/instructor-workspace.service';

@Component({
  selector: 'app-instructor-lecture-list',
  imports: [ReactiveFormsModule],
  templateUrl: './instructor-lecture-list.html',
  styleUrl: './instructor-lecture-list.css',
})
export class InstructorLectureList implements OnInit {
  private readonly workspaceService = inject(InstructorWorkspaceService);
  private readonly router = inject(Router);

  protected readonly status = signal('Ready');
  protected readonly lectures = signal<InstructorLectureSummaryResponse[]>([]);

  readonly createLectureForm = new FormGroup({
    title: new FormControl('', [Validators.required, Validators.minLength(3)]),
  });

  async ngOnInit() {
    await this.refreshLectures();
  }

  async createLecture() {
    if (this.createLectureForm.invalid) {
      return;
    }

    let lectureId = '';

    try {
      lectureId = await this.workspaceService.createLecture(this.createLectureForm.value.title!);
      this.createLectureForm.reset({ title: '' });
    } catch {
      this.status.set('Could not create lecture. Please retry.');
      return;
    }

    const refreshed = await this.refreshLectures({ preserveStatusOnError: true });
    this.status.set(
      refreshed
        ? `Lecture created: ${lectureId}`
        : `Lecture created: ${lectureId}, but list refresh failed.`,
    );
    await this.openLecture(lectureId);
  }

  async openLecture(lectureId: string) {
    await this.router.navigate(['/instructor/lectures', lectureId]);
  }

  async refreshLectures(options?: { preserveStatusOnError?: boolean }) {
    try {
      const lectures = await this.workspaceService.listLectures();
      this.lectures.set(lectures);
      if (!options?.preserveStatusOnError) {
        this.status.set(lectures.length > 0 ? 'Lectures loaded' : 'No lectures yet. Create your first lecture.');
      }
      return true;
    } catch {
      if (!options?.preserveStatusOnError) {
        this.status.set('Could not load lectures. Please retry.');
      }
      return false;
    }
  }
}
