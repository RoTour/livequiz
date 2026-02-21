import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { InstructorHome } from './instructor-home';
import { InstructorWorkspaceService } from './application/instructor-workspace.service';

describe('InstructorHome', () => {
  let component: InstructorHome;
  let fixture: ComponentFixture<InstructorHome>;

  const createLecture = vi.fn();
  const addQuestion = vi.fn();
  const unlockQuestion = vi.fn();
  const unlockNextQuestion = vi.fn();
  const getLectureState = vi.fn();
  const createInvite = vi.fn();
  const listInvites = vi.fn();
  const revokeInvite = vi.fn();

  beforeEach(async () => {
    createLecture.mockReset();
    addQuestion.mockReset();
    unlockQuestion.mockReset();
    unlockNextQuestion.mockReset();
    getLectureState.mockReset();
    createInvite.mockReset();
    listInvites.mockReset();
    revokeInvite.mockReset();

    listInvites.mockResolvedValue([]);

    await TestBed.configureTestingModule({
      imports: [InstructorHome],
      providers: [
        {
          provide: InstructorWorkspaceService,
          useValue: {
            createLecture,
            addQuestion,
            unlockQuestion,
            unlockNextQuestion,
            getLectureState,
            createInvite,
            listInvites,
            revokeInvite,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InstructorHome);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('creates lecture and renders lecture id', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    component.createLectureForm.setValue({ title: 'DDD' });

    await component.createLecture();
    fixture.detectChanges();

    expect(createLecture).toHaveBeenCalledWith('DDD');
    expect(getLectureState).toHaveBeenCalledWith('lecture-1');
    expect(listInvites).toHaveBeenCalledWith('lecture-1');
    expect(fixture.nativeElement.textContent).toContain('lecture-1');
  });

  it('adds question and refreshes lecture state', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState
      .mockResolvedValueOnce({ lectureId: 'lecture-1', title: 'DDD', questions: [] })
      .mockResolvedValueOnce({
        lectureId: 'lecture-1',
        title: 'DDD',
        questions: [
          {
            questionId: 'q-1',
            prompt: 'What is an aggregate?',
            order: 1,
            timeLimitSeconds: 60,
            unlocked: false,
          },
        ],
      });
    await setupLecture(component);

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
    expect(getLectureState).toHaveBeenCalledTimes(2);
    expect(component.addQuestionForm.value.prompt).toBe('');
  });

  it('unlocks next question and updates status', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    await setupLecture(component);

    await component.unlockNextQuestion();
    fixture.detectChanges();

    expect(unlockNextQuestion).toHaveBeenCalledWith('lecture-1');
    expect(fixture.nativeElement.textContent).toContain('Unlocked next question');
  });

  it('unlocks specific question and refreshes lecture state', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    await setupLecture(component);

    await component.unlockQuestion('q-2');

    expect(unlockQuestion).toHaveBeenCalledWith('lecture-1', 'q-2');
    expect(getLectureState).toHaveBeenCalledTimes(2);
  });

  it('creates invite then refreshes invite list', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
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
      ]);
    await setupLecture(component);

    await component.createInvite();
    fixture.detectChanges();

    expect(createInvite).toHaveBeenCalledWith('lecture-1');
    expect(listInvites).toHaveBeenCalledTimes(2);
    expect(fixture.nativeElement.textContent).toContain('ABCD12');
  });

  it('revokes invite and refreshes list', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    listInvites
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([
        {
          inviteId: 'inv-1',
          lectureId: 'lecture-1',
          joinCode: 'ABCD12',
          createdAt: '2026-02-19T08:00:00Z',
          expiresAt: '2026-02-19T10:00:00Z',
          revokedAt: '2026-02-19T09:00:00Z',
          active: false,
        },
      ]);
    await setupLecture(component);

    await component.revokeInvite('inv-1');

    expect(revokeInvite).toHaveBeenCalledWith('lecture-1', 'inv-1');
    expect(listInvites).toHaveBeenCalledTimes(2);
  });

  it('does not create lecture when form is invalid', async () => {
    component.createLectureForm.setValue({ title: '' });

    await component.createLecture();

    expect(createLecture).not.toHaveBeenCalled();
  });

  it('shows actionable message when lecture creation fails', async () => {
    createLecture.mockRejectedValue(new Error('create failed'));
    component.createLectureForm.setValue({ title: 'DDD' });

    await component.createLecture();
    fixture.detectChanges();

    expect(getLectureState).not.toHaveBeenCalled();
    expect(listInvites).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Could not create lecture. Please retry.');
  });

  it('shows actionable message when adding question fails', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    addQuestion.mockRejectedValue(new Error('add question failed'));
    await setupLecture(component);
    addQuestion.mockClear();
    getLectureState.mockClear();

    component.addQuestionForm.setValue({
      prompt: 'What is an aggregate?',
      modelAnswer: 'Boundary of consistency',
      timeLimitSeconds: 60,
    });

    await component.addQuestion();
    fixture.detectChanges();

    expect(addQuestion).toHaveBeenCalledTimes(1);
    expect(getLectureState).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Could not add question. Please retry.');
  });

  it('shows invite refresh error when loading invites fails', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    listInvites
      .mockResolvedValueOnce([])
      .mockRejectedValueOnce(new Error('list failed'));
    await setupLecture(component);

    await component.refreshInvites();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Could not refresh invites. Please retry.');
  });

  it('shows state refresh error when loading lecture state fails', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockResolvedValue({ lectureId: 'lecture-1', title: 'DDD', questions: [] });
    await setupLecture(component);
    getLectureState.mockRejectedValueOnce(new Error('state failed'));

    await component.refreshLectureState();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Could not refresh lecture state. Please retry.');
  });

  it('keeps mutation success message when refresh fails after create', async () => {
    createLecture.mockResolvedValue('lecture-1');
    getLectureState.mockRejectedValue(new Error('state endpoint unavailable'));
    component.createLectureForm.setValue({ title: 'DDD' });

    await component.createLecture();
    fixture.detectChanges();

    expect(createLecture).toHaveBeenCalledWith('DDD');
    expect(getLectureState).toHaveBeenCalledWith('lecture-1');
    expect(fixture.nativeElement.textContent).toContain(
      'Lecture created: lecture-1, but latest state could not be loaded.',
    );
  });

  it('clears previously created invite summary when creating a new lecture', async () => {
    createLecture.mockResolvedValueOnce('lecture-1').mockResolvedValueOnce('lecture-2');
    getLectureState
      .mockResolvedValueOnce({ lectureId: 'lecture-1', title: 'DDD', questions: [] })
      .mockResolvedValueOnce({ lectureId: 'lecture-2', title: 'Tactical DDD', questions: [] });
    createInvite.mockResolvedValue({
      inviteId: 'inv-1',
      lectureId: 'lecture-1',
      joinCode: 'ABCD12',
      joinUrl: 'https://join/token',
      expiresAt: '2026-02-19T10:00:00Z',
      active: true,
    });
    listInvites.mockResolvedValue([]);

    component.createLectureForm.setValue({ title: 'DDD' });
    await component.createLecture();
    await component.createInvite();
    fixture.detectChanges();
    expect(fixture.nativeElement.textContent).toContain('New code:');

    component.createLectureForm.setValue({ title: 'Tactical DDD' });
    await component.createLecture();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('New code:');
    expect(fixture.nativeElement.textContent).toContain('lecture-2');
  });
});

async function setupLecture(component: InstructorHome) {
  component.createLectureForm.setValue({ title: 'DDD' });
  await component.createLecture();
}
