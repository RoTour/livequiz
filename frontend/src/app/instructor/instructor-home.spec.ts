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

  beforeEach(async () => {
    createLecture.mockReset();
    addQuestion.mockReset();
    unlockQuestion.mockReset();
    unlockNextQuestion.mockReset();
    getLectureState.mockReset();

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

  it('does not create lecture when form is invalid', async () => {
    component.createLectureForm.setValue({ title: '' });

    await component.createLecture();

    expect(createLecture).not.toHaveBeenCalled();
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
});

async function setupLecture(component: InstructorHome) {
  component.createLectureForm.setValue({ title: 'DDD' });
  await component.createLecture();
}
