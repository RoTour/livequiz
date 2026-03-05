import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { LectureStatePanel } from './lecture-state-panel';

describe('LectureStatePanel', () => {
  let fixture: ComponentFixture<LectureStatePanel>;

  const setDefaultInputs = () => {
    fixture.componentRef.setInput('lectureState', null);
    fixture.componentRef.setInput('questionAnalytics', []);
    fixture.componentRef.setInput('analyticsLoading', false);
    fixture.componentRef.setInput('analyticsError', '');
    fixture.componentRef.setInput('selectedHistoryQuestionId', '');
    fixture.componentRef.setInput('questionReviews', []);
    fixture.componentRef.setInput('questionHistoryLoading', false);
    fixture.componentRef.setInput('questionHistoryError', '');
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LectureStatePanel],
    }).compileComponents();

    fixture = TestBed.createComponent(LectureStatePanel);
    setDefaultInputs();
    fixture.detectChanges();
  });

  it('shows an empty-state message when no lecture is selected', () => {
    expect(fixture.nativeElement.textContent).toContain('No lecture selected yet.');
  });

  it('renders question status, analytics, and emits question actions', () => {
    const unlockQuestion = vi.fn();
    const openHistory = vi.fn();
    fixture.componentInstance.unlockQuestion.subscribe(unlockQuestion);
    fixture.componentInstance.openHistory.subscribe(openHistory);

    fixture.componentRef.setInput('lectureState', {
      lectureId: 'lecture-1',
      title: 'DDD Session',
      questions: [
        {
          questionId: 'q-1',
          prompt: 'Define aggregate root',
          order: 1,
          timeLimitSeconds: 60,
          unlocked: false,
        },
        {
          questionId: 'q-2',
          prompt: 'Explain bounded context',
          order: 2,
          timeLimitSeconds: 60,
          unlocked: true,
        },
      ],
    });
    fixture.componentRef.setInput('questionAnalytics', [
      {
        questionId: 'q-1',
        prompt: 'Define aggregate root',
        order: 1,
        enrolledCount: 2,
        answeredCount: 1,
        unansweredCount: 1,
        multiAttemptCount: 1,
      },
    ]);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('DDD Session');
    expect(compiled.textContent).toContain('locked');
    expect(compiled.textContent).toContain('unlocked');
    expect(compiled.textContent).toContain('Enrolled 2');
    expect(compiled.textContent).toContain('Answered 1');

    const unlockButtons = Array.from(compiled.querySelectorAll('.lq-question-item button')) as HTMLButtonElement[];
    expect(unlockButtons).toHaveLength(4);
    expect(unlockButtons[0].disabled).toBe(false);
    expect(unlockButtons[2].disabled).toBe(true);

    unlockButtons[0].click();
    unlockButtons[1].click();
    expect(unlockQuestion).toHaveBeenCalledWith('q-1');
    expect(openHistory).toHaveBeenCalledWith('q-1');
  });

  it('shows loading and error states for analytics and history', () => {
    fixture.componentRef.setInput('lectureState', {
      lectureId: 'lecture-1',
      title: 'DDD Session',
      questions: [
        {
          questionId: 'q-1',
          prompt: 'Define aggregate root',
          order: 1,
          timeLimitSeconds: 60,
          unlocked: true,
        },
      ],
    });
    fixture.componentRef.setInput('analyticsLoading', true);
    fixture.componentRef.setInput('analyticsError', 'Could not load analytics.');
    fixture.componentRef.setInput('selectedHistoryQuestionId', 'q-1');
    fixture.componentRef.setInput('questionHistoryLoading', true);
    fixture.componentRef.setInput('questionHistoryError', 'Could not load history.');
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Loading analytics…');
    expect(compiled.textContent).toContain('Could not load analytics.');
    expect(compiled.textContent).toContain('Loading student answers…');
  });

  it('prefers verified email and renders attempt-level review details', () => {
    fixture.componentRef.setInput('lectureState', {
      lectureId: 'lecture-1',
      title: 'DDD Session',
      questions: [
        {
          questionId: 'q-1',
          prompt: 'Define aggregate root',
          order: 1,
          timeLimitSeconds: 60,
          unlocked: true,
        },
      ],
    });
    fixture.componentRef.setInput('selectedHistoryQuestionId', 'q-1');
    fixture.componentRef.setInput('questionReviews', [
      {
        studentId: 'uuid-1',
        studentEmail: 'student@example.com',
        attempts: [
          {
            submissionId: 'submission-1',
            answeredAt: '2026-02-21T11:30:00Z',
            answerText: 'Second attempt',
            reviewStatus: 'NEEDS_IMPROVEMENT',
            reviewPublished: false,
            reviewComment: 'Missing key points.',
            reviewUpdatedAt: null,
            reviewCreatedAt: null,
            reviewPublishedAt: null,
            reviewedByInstructorId: null,
            reviewOrigin: null,
            llmSuggestedStatus: 'INCOMPLETE',
            llmSuggestedComment: 'Needs more precision.',
            llmSuggestedAt: '2026-02-21T11:40:00Z',
            llmSuggestedModel: 'openai/gpt-4o-mini',
            llmAcceptedAt: null,
            llmAcceptedByInstructorId: null,
          },
        ],
      },
      {
        studentId: 'uuid-2',
        studentEmail: null,
        attempts: [],
      },
    ]);
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('student@example.com');
    expect(compiled.textContent).toContain('Student ID');
    expect(compiled.textContent).toContain('No submission yet.');
    expect(compiled.textContent).toContain('Second attempt');
    expect(compiled.textContent).toContain('Accept AI review');
    expect(compiled.textContent).not.toContain('2026-02-21T11:30:00Z');
  });
});
