import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import QRCode from 'qrcode';
import { defer, of } from 'rxjs';
import { vi } from 'vitest';
import { InstructorHome } from './instructor-home';
import { InstructorWorkspaceService } from './application/instructor-workspace.service';

describe('InstructorHome', () => {
  let component: InstructorHome;
  let fixture: ComponentFixture<InstructorHome>;
  let qrPreviewWindow: Window;

  const addQuestion = vi.fn();
  const unlockQuestion = vi.fn();
  const unlockNextQuestion = vi.fn();
  const getLectureState = vi.fn();
  const listQuestionAnalytics = vi.fn();
  const listQuestionAnswerHistory = vi.fn();
  const createInvite = vi.fn();
  const listInvites = vi.fn();
  const revokeInvite = vi.fn();
  let lectureIdParam: string | null = 'lecture-1';

  afterEach(() => {
    window.localStorage.clear();
    vi.restoreAllMocks();
  });

  beforeEach(async () => {
    qrPreviewWindow = {
      closed: false,
      location: {
        href: '',
      } as Location,
      close: vi.fn(),
    } as unknown as Window;

    const qrCode = QRCode as unknown as { toDataURL: (...args: unknown[]) => Promise<string> };
    vi.spyOn(qrCode, 'toDataURL').mockResolvedValue('data:image/png;base64,qr-test');
    vi.spyOn(window, 'open').mockReturnValue(qrPreviewWindow);

    addQuestion.mockReset();
    unlockQuestion.mockReset();
    unlockNextQuestion.mockReset();
    getLectureState.mockReset();
    listQuestionAnalytics.mockReset();
    listQuestionAnswerHistory.mockReset();
    createInvite.mockReset();
    listInvites.mockReset();
    revokeInvite.mockReset();
    lectureIdParam = 'lecture-1';

    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    listQuestionAnalytics.mockResolvedValue([]);
    listQuestionAnswerHistory.mockResolvedValue([]);
    listInvites.mockResolvedValue([]);

    await TestBed.configureTestingModule({
      imports: [InstructorHome],
      providers: [
        provideRouter([]),
        {
          provide: InstructorWorkspaceService,
          useValue: {
            addQuestion,
            unlockQuestion,
            unlockNextQuestion,
            getLectureState,
            listQuestionAnalytics,
            listQuestionAnswerHistory,
            createInvite,
            listInvites,
            revokeInvite,
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
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InstructorHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('hydrates lecture context from route param', () => {
    expect(getLectureState).toHaveBeenCalledWith('lecture-1');
    expect(listQuestionAnalytics).toHaveBeenCalledWith('lecture-1');
    expect(listInvites).toHaveBeenCalledWith('lecture-1');
    expect(fixture.nativeElement.textContent).toContain('DDD');
  });

  it('shows guidance when lecture route param is missing', async () => {
    lectureIdParam = null;
    getLectureState.mockClear();
    listQuestionAnalytics.mockClear();
    listInvites.mockClear();
    fixture = TestBed.createComponent(InstructorHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(getLectureState).not.toHaveBeenCalled();
    expect(listQuestionAnalytics).not.toHaveBeenCalled();
    expect(listInvites).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Lecture not selected. Return to the lecture list.');
  });

  it('loads student answer history for selected question', async () => {
    listQuestionAnswerHistory.mockResolvedValueOnce([
      {
        studentId: 'student',
        studentEmail: 'student@example.com',
        latestAnswerAt: '2026-02-21T11:30:00Z',
        attemptCount: 2,
        latestAnswerText: 'Second attempt',
      },
    ]);

    await component.openQuestionAnswerHistory('question-1');
    fixture.detectChanges();

    expect(listQuestionAnswerHistory).toHaveBeenCalledWith('lecture-1', 'question-1');
  });

  it('adds question and refreshes lecture state', async () => {
    getLectureState.mockResolvedValueOnce({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    component.addQuestionForm.setValue({
      prompt: 'What is an aggregate?',
      modelAnswer: 'Boundary of consistency',
      timeLimitSeconds: 60,
    });

    await component.addQuestion();

    expect(addQuestion).toHaveBeenCalledWith('lecture-1', {
      prompt: 'What is an aggregate?',
      modelAnswer: 'Boundary of consistency',
      timeLimitSeconds: 60,
    });
    expect(getLectureState).toHaveBeenCalled();
  });

  it('unlocks next question and updates status', async () => {
    await component.unlockNextQuestion();
    fixture.detectChanges();

    expect(unlockNextQuestion).toHaveBeenCalledWith('lecture-1');
    expect(getLectureState).toHaveBeenCalled();
  });

  it('creates and revokes invites with list refresh', async () => {
    createInvite.mockResolvedValue({
      inviteId: 'inv-1',
      lectureId: 'lecture-1',
      joinCode: 'ABCD12',
      joinUrl: 'https://join/token',
      expiresAt: '2026-02-19T10:00:00Z',
      active: true,
    });
    listInvites
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        {
          inviteId: 'inv-1',
          lectureId: 'lecture-1',
          joinCode: 'ABCD12',
          createdAt: '2026-02-19T08:00:00Z',
          expiresAt: '2026-02-19T10:00:00Z',
          revokedAt: null,
          active: true,
        },
      ])
      .mockResolvedValueOnce([]);

    await component.createInvite();
    await component.revokeInvite('inv-1');

    expect(createInvite).toHaveBeenCalledWith('lecture-1');
    expect(window.open).toHaveBeenCalledWith('', '_blank');
    expect(qrPreviewWindow.location.href.startsWith('/instructor/invites/inv-1/qr#')).toBe(true);
    expect(revokeInvite).toHaveBeenCalledWith('lecture-1', 'inv-1');
    expect(listInvites).toHaveBeenCalled();
  });

  it('shows actionable errors for failed mutations and refreshes', async () => {
    addQuestion.mockRejectedValue(new Error('add failed'));
    component.addQuestionForm.setValue({
      prompt: 'What is an aggregate?',
      modelAnswer: 'Boundary of consistency',
      timeLimitSeconds: 60,
    });

    await component.addQuestion();
    fixture.detectChanges();
    expect(addQuestion).toHaveBeenCalledTimes(1);

    listInvites.mockRejectedValueOnce(new Error('list failed'));
    await component.refreshInvites();
    fixture.detectChanges();
    expect(listInvites).toHaveBeenCalled();

    listQuestionAnalytics.mockRejectedValueOnce(new Error('analytics failed'));
    await component.refreshQuestionAnalytics();
    fixture.detectChanges();
    expect(listQuestionAnalytics).toHaveBeenCalled();

    getLectureState.mockRejectedValueOnce(new Error('state failed'));
    await component.refreshLectureState();
    fixture.detectChanges();
    expect(getLectureState).toHaveBeenCalled();
  });
});
