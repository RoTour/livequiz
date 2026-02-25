import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { defer, of } from 'rxjs';
import { vi } from 'vitest';
import { StudentWorkspaceService } from './application/student-workspace.service';
import { StudentLectureRoom } from './student-lecture-room';
import { AuthService } from '../login/auth.service';

describe('StudentLectureRoom', () => {
  let fixture: ComponentFixture<StudentLectureRoom>;
  let component: StudentLectureRoom;

  const getNextQuestion = vi.fn();
  const getAnswerStatuses = vi.fn();
  const submitAnswer = vi.fn();
  let lectureIdParam: string | null = 'lecture-1';

  beforeEach(async () => {
    getNextQuestion.mockReset();
    getAnswerStatuses.mockReset();
    submitAnswer.mockReset();
    lectureIdParam = 'lecture-1';

    getNextQuestion.mockResolvedValue({
      hasQuestion: true,
      lectureId: 'lecture-1',
      questionId: 'q-1',
      prompt: 'Explain aggregate boundary',
      order: 1,
      timeLimitSeconds: 60,
    });
    getAnswerStatuses.mockResolvedValue([]);

    await TestBed.configureTestingModule({
      imports: [StudentLectureRoom],
      providers: [
        {
          provide: StudentWorkspaceService,
          useValue: {
              getNextQuestion,
              getAnswerStatuses,
              submitAnswer,
            },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              get paramMap() {
                return convertToParamMap({ lectureId: lectureIdParam ?? undefined });
              },
            },
            paramMap: defer(() => of(convertToParamMap({ lectureId: lectureIdParam ?? undefined }))),
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

    fixture = TestBed.createComponent(StudentLectureRoom);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('hydrates lecture room from route param and loads next question', async () => {
    await component.loadNextQuestion();
    fixture.detectChanges();

    expect(getNextQuestion).toHaveBeenCalledWith('lecture-1');
    expect(fixture.nativeElement.textContent).toContain('Explain aggregate boundary');
  });

  it('shows guidance when lecture route param is missing', async () => {
    lectureIdParam = null;
    getNextQuestion.mockClear();

    fixture = TestBed.createComponent(StudentLectureRoom);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(getNextQuestion).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Lecture not selected. Return to your lecture list.');
  });

  it('shows cooldown feedback on throttled submission', async () => {
    submitAnswer.mockRejectedValue({
      status: 429,
      error: {
        details: { retryAfterSeconds: 12 },
      },
    });

    component.submitAnswerForm.setValue({ answerText: 'Aggregate enforces invariants.' });
    await component.submitAnswer();
    fixture.detectChanges();

    expect(submitAnswer).toHaveBeenCalledWith('lecture-1', {
      questionId: 'q-1',
      answerText: 'Aggregate enforces invariants.',
    });
    expect(fixture.nativeElement.textContent).toContain('Cooldown active: retry in 12s');
  });

  it('shows enrollment-required message when next-question lookup is forbidden', async () => {
    getNextQuestion.mockRejectedValueOnce({
      error: {
        code: 'LECTURE_ENROLLMENT_REQUIRED',
      },
    });

    await component.loadNextQuestion();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Enrollment required before loading questions.');
  });
});
