import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { defer, of } from 'rxjs';
import { vi } from 'vitest';
import { InstructorHome } from './instructor-home';
import { InstructorWorkspaceService } from './application/instructor-workspace.service';

describe('InstructorHome', () => {
  let component: InstructorHome;
  let fixture: ComponentFixture<InstructorHome>;

  const addQuestion = vi.fn();
  const unlockQuestion = vi.fn();
  const unlockNextQuestion = vi.fn();
  const getLectureState = vi.fn();
  const createInvite = vi.fn();
  const listInvites = vi.fn();
  const revokeInvite = vi.fn();
  let lectureIdParam: string | null = 'lecture-1';

  beforeEach(async () => {
    addQuestion.mockReset();
    unlockQuestion.mockReset();
    unlockNextQuestion.mockReset();
    getLectureState.mockReset();
    createInvite.mockReset();
    listInvites.mockReset();
    revokeInvite.mockReset();
    lectureIdParam = 'lecture-1';

    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    listInvites.mockResolvedValue([]);

    await TestBed.configureTestingModule({
      imports: [InstructorHome],
      providers: [
        {
          provide: InstructorWorkspaceService,
          useValue: {
            addQuestion,
            unlockQuestion,
            unlockNextQuestion,
            getLectureState,
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
    expect(listInvites).toHaveBeenCalledWith('lecture-1');
    expect(fixture.nativeElement.textContent).toContain('DDD');
  });

  it('shows guidance when lecture route param is missing', async () => {
    lectureIdParam = null;
    getLectureState.mockClear();
    listInvites.mockClear();
    fixture = TestBed.createComponent(InstructorHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(getLectureState).not.toHaveBeenCalled();
    expect(listInvites).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Lecture not selected. Return to the lecture list.');
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

    getLectureState.mockRejectedValueOnce(new Error('state failed'));
    await component.refreshLectureState();
    fixture.detectChanges();
    expect(getLectureState).toHaveBeenCalled();
  });
});
