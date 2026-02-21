import { ComponentFixture, TestBed } from '@angular/core/testing';
import { vi } from 'vitest';
import { LectureStatePanel } from './lecture-state-panel';

describe('LectureStatePanel', () => {
  let fixture: ComponentFixture<LectureStatePanel>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LectureStatePanel],
    }).compileComponents();

    fixture = TestBed.createComponent(LectureStatePanel);
    fixture.componentRef.setInput('lectureState', null);
    fixture.detectChanges();
  });

  it('shows an empty-state message when no lecture is selected', () => {
    expect(fixture.nativeElement.textContent).toContain('No lecture selected yet.');
  });

  it('renders question status and emits unlock for locked question', () => {
    const unlockQuestion = vi.fn();
    fixture.componentInstance.unlockQuestion.subscribe(unlockQuestion);

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
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('DDD Session');
    expect(compiled.textContent).toContain('locked');
    expect(compiled.textContent).toContain('unlocked');

    const unlockButtons = Array.from(compiled.querySelectorAll('.lq-question-item button')) as HTMLButtonElement[];
    expect(unlockButtons).toHaveLength(2);
    expect(unlockButtons[0].disabled).toBe(false);
    expect(unlockButtons[1].disabled).toBe(true);

    unlockButtons[0].click();
    expect(unlockQuestion).toHaveBeenCalledWith('q-1');
  });
});
