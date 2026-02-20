import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { vi } from 'vitest';
import { StudentHome } from './student-home';
import { StudentWorkspaceService } from './application/student-workspace.service';

describe('StudentHome', () => {
  let component: StudentHome;
  let fixture: ComponentFixture<StudentHome>;

  const joinLectureByCode = vi.fn();
  const getNextQuestion = vi.fn();
  const submitAnswer = vi.fn();
  let queryParamMap = convertToParamMap({});

  beforeEach(async () => {
    joinLectureByCode.mockReset();
    getNextQuestion.mockReset();
    submitAnswer.mockReset();
    queryParamMap = convertToParamMap({});

    await TestBed.configureTestingModule({
      imports: [StudentHome],
      providers: [
        {
          provide: StudentWorkspaceService,
          useValue: {
            joinLectureByCode,
            getNextQuestion,
            submitAnswer,
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              get queryParamMap() {
                return queryParamMap;
              },
            },
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(StudentHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('joins lecture by invite code and renders lecture id', async () => {
    joinLectureByCode.mockResolvedValue({
      lectureId: 'lecture-1',
      studentId: 'student-1',
      alreadyEnrolled: false,
      enrolledAt: '2026-02-19T10:00:00Z',
    });
    component.joinLectureForm.setValue({ code: 'abc123' });

    await component.joinLecture();
    fixture.detectChanges();

    expect(joinLectureByCode).toHaveBeenCalledWith('ABC123');
    expect(fixture.nativeElement.textContent).toContain('lecture-1');
    expect(fixture.nativeElement.textContent).toContain('Enrolled successfully');
  });

  it('loads next question after lecture is selected', async () => {
    joinLectureByCode.mockResolvedValue({
      lectureId: 'lecture-1',
      studentId: 'student-1',
      alreadyEnrolled: true,
      enrolledAt: '2026-02-19T10:00:00Z',
    });
    getNextQuestion.mockResolvedValue({
      hasQuestion: true,
      lectureId: 'lecture-1',
      questionId: 'q-1',
      prompt: 'Explain DDD aggregate root',
      order: 1,
      timeLimitSeconds: 60,
    });
    component.joinLectureForm.setValue({ code: 'abc123' });
    await component.joinLecture();

    await component.loadNextQuestion();
    fixture.detectChanges();

    expect(getNextQuestion).toHaveBeenCalledWith('lecture-1');
    expect(fixture.nativeElement.textContent).toContain('Explain DDD aggregate root');
  });

  it('submits answer and reloads next question', async () => {
    joinLectureByCode.mockResolvedValue({
      lectureId: 'lecture-1',
      studentId: 'student-1',
      alreadyEnrolled: true,
      enrolledAt: '2026-02-19T10:00:00Z',
    });
    getNextQuestion
      .mockResolvedValueOnce({
        hasQuestion: true,
        lectureId: 'lecture-1',
        questionId: 'q-1',
        prompt: 'Explain DDD aggregate root',
        order: 1,
        timeLimitSeconds: 60,
      })
      .mockResolvedValueOnce({ hasQuestion: false });
    submitAnswer.mockResolvedValue({
      submissionId: 'sub-1',
      lectureId: 'lecture-1',
      questionId: 'q-1',
      studentId: 'student-1',
    });

    component.joinLectureForm.setValue({ code: 'abc123' });
    await component.joinLecture();
    await component.loadNextQuestion();
    component.submitAnswerForm.setValue({ answerText: 'Aggregate root coordinates invariants.' });

    await component.submitAnswer();

    expect(submitAnswer).toHaveBeenCalledWith('lecture-1', {
      questionId: 'q-1',
      answerText: 'Aggregate root coordinates invariants.',
    });
    expect(getNextQuestion).toHaveBeenCalledTimes(2);
    expect(component.submitAnswerForm.value.answerText).toBe('');
  });

  it('shows cooldown message when submission is throttled', async () => {
    joinLectureByCode.mockResolvedValue({
      lectureId: 'lecture-1',
      studentId: 'student-1',
      alreadyEnrolled: true,
      enrolledAt: '2026-02-19T10:00:00Z',
    });
    getNextQuestion.mockResolvedValue({
      hasQuestion: true,
      lectureId: 'lecture-1',
      questionId: 'q-1',
      prompt: 'Explain DDD aggregate root',
      order: 1,
      timeLimitSeconds: 60,
    });
    submitAnswer.mockRejectedValue({
      status: 429,
      error: {
        details: { retryAfterSeconds: 12 },
      },
    });

    component.joinLectureForm.setValue({ code: 'abc123' });
    await component.joinLecture();
    await component.loadNextQuestion();
    component.submitAnswerForm.setValue({ answerText: 'Aggregate root coordinates invariants.' });

    await component.submitAnswer();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Cooldown active: retry in 12s');
  });

  it('clears previous lecture question state when joining a new lecture', async () => {
    joinLectureByCode
      .mockResolvedValueOnce({
        lectureId: 'lecture-1',
        studentId: 'student-1',
        alreadyEnrolled: false,
        enrolledAt: '2026-02-19T10:00:00Z',
      })
      .mockResolvedValueOnce({
        lectureId: 'lecture-2',
        studentId: 'student-1',
        alreadyEnrolled: false,
        enrolledAt: '2026-02-19T10:05:00Z',
      });
    getNextQuestion.mockResolvedValue({
      hasQuestion: true,
      lectureId: 'lecture-1',
      questionId: 'q-1',
      prompt: 'Question from first lecture',
      order: 1,
      timeLimitSeconds: 60,
    });

    component.joinLectureForm.setValue({ code: 'abc123' });
    await component.joinLecture();
    await component.loadNextQuestion();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('Question from first lecture');

    component.joinLectureForm.setValue({ code: 'def456' });
    await component.joinLecture();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Question from first lecture');
    expect(component.submitAnswerForm.value.answerText).toBe('');

    await component.submitAnswer();
    expect(submitAnswer).not.toHaveBeenCalled();
  });

  it('hydrates selected lecture from invite deep-link query params', async () => {
    queryParamMap = convertToParamMap({
      lectureId: 'lecture-9',
      alreadyEnrolled: '1',
    });

    fixture = TestBed.createComponent(StudentHome);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('lecture-9');
    expect(fixture.nativeElement.textContent).toContain('Already enrolled');
    expect(fixture.nativeElement.textContent).toContain('Lecture joined from invite link');
  });
});
