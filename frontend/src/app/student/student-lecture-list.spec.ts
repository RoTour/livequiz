import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { Router } from '@angular/router';
import { vi } from 'vitest';
import { StudentWorkspaceService } from './application/student-workspace.service';
import { StudentLectureList } from './student-lecture-list';
import { ToastService } from '../shared/toast/toast.service';
import { AuthService } from '../login/auth.service';

describe('StudentLectureList', () => {
  let fixture: ComponentFixture<StudentLectureList>;
  let component: StudentLectureList;

  const listLectures = vi.fn();
  const joinLectureByCode = vi.fn();
  const navigate = vi.fn();
  const show = vi.fn();

  beforeEach(async () => {
    listLectures.mockReset();
    joinLectureByCode.mockReset();
    navigate.mockReset();
    show.mockReset();

    listLectures.mockResolvedValue([]);

    await TestBed.configureTestingModule({
      imports: [StudentLectureList],
      providers: [
        {
          provide: StudentWorkspaceService,
          useValue: {
            listLectures,
            joinLectureByCode,
          },
        },
        {
          provide: Router,
          useValue: {
            navigate,
          },
        },
        {
          provide: ToastService,
          useValue: {
            show,
          },
        },
        {
          provide: AuthService,
          useValue: {
            isAnonymousStudent: signal(true).asReadonly(),
            isStudentEmailVerified: signal(false).asReadonly(),
            registerStudentEmail: vi.fn(),
            resendStudentVerification: vi.fn(),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(StudentLectureList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('loads and renders joined lectures', async () => {
    listLectures.mockResolvedValue([
      {
        lectureId: 'lecture-1',
        title: 'Distributed Systems',
        enrolledAt: '2026-02-21T10:00:00Z',
        questionCount: 4,
        answeredCount: 2,
      },
    ]);

    await component.refreshLectures();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Distributed Systems');
    expect(fixture.nativeElement.textContent).toContain('2/4 answered');
  });

  it('joins lecture by code and navigates into lecture room', async () => {
    joinLectureByCode.mockResolvedValue({
      lectureId: 'lecture-5',
      studentId: 'student',
      alreadyEnrolled: false,
      enrolledAt: '2026-02-21T10:00:00Z',
    });
    listLectures.mockResolvedValue([]);
    navigate.mockResolvedValue(true);
    component.joinLectureForm.setValue({ code: 'ab12cd' });

    await component.joinLecture();

    expect(joinLectureByCode).toHaveBeenCalledWith('AB12CD');
    expect(navigate).toHaveBeenCalledWith(['/student/lectures', 'lecture-5']);
  });

  it('opens existing lecture from list', async () => {
    navigate.mockResolvedValue(true);

    await component.openLecture('lecture-9');

    expect(navigate).toHaveBeenCalledWith(['/student/lectures', 'lecture-9']);
  });
});
