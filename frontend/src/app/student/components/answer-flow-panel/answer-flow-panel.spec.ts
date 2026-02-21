import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { vi } from 'vitest';
import { AnswerFlowPanel } from './answer-flow-panel';

describe('AnswerFlowPanel', () => {
  let fixture: ComponentFixture<AnswerFlowPanel>;
  let submitAnswerForm: FormGroup;

  beforeEach(async () => {
    submitAnswerForm = new FormGroup({
      answerText: new FormControl('', [Validators.required, Validators.minLength(2)]),
    });

    await TestBed.configureTestingModule({
      imports: [AnswerFlowPanel],
    }).compileComponents();

    fixture = TestBed.createComponent(AnswerFlowPanel);
    fixture.componentRef.setInput('selectedLectureId', '');
    fixture.componentRef.setInput('nextQuestion', null);
    fixture.componentRef.setInput('submitAnswerForm', submitAnswerForm);
    fixture.componentRef.setInput('cooldownMessage', '');
    fixture.detectChanges();
  });

  it('emits load-next action and disables it when no lecture is selected', () => {
    const loadNextQuestion = vi.fn();
    fixture.componentInstance.loadNextQuestion.subscribe(loadNextQuestion);

    const loadButton = findButton(fixture, 'Load next question');
    expect(loadButton.disabled).toBe(true);

    fixture.componentRef.setInput('selectedLectureId', 'lecture-1');
    fixture.detectChanges();

    findButton(fixture, 'Load next question').click();
    expect(loadNextQuestion).toHaveBeenCalledTimes(1);
  });

  it('renders answer form for available question and emits submit', () => {
    const submitAnswer = vi.fn();
    fixture.componentInstance.submitAnswer.subscribe(submitAnswer);

    fixture.componentRef.setInput('selectedLectureId', 'lecture-1');
    fixture.componentRef.setInput('nextQuestion', {
      hasQuestion: true,
      lectureId: 'lecture-1',
      questionId: 'q-1',
      prompt: 'Explain aggregate root',
      order: 1,
      timeLimitSeconds: 60,
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Explain aggregate root');

    submitAnswerForm.setValue({ answerText: 'It enforces aggregate invariants.' });
    fixture.detectChanges();

    const submitButton = findButton(fixture, 'Submit answer');
    expect(submitButton.disabled).toBe(false);
    submitButton.click();

    expect(submitAnswer).toHaveBeenCalledTimes(1);
  });

  it('renders empty question state and cooldown message', () => {
    fixture.componentRef.setInput('selectedLectureId', 'lecture-1');
    fixture.componentRef.setInput('nextQuestion', { hasQuestion: false });
    fixture.componentRef.setInput('cooldownMessage', 'Cooldown active: retry in 8s');
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('No unlocked pending question.');
    expect(compiled.textContent).toContain('Cooldown active: retry in 8s');
  });
});

function findButton(fixture: ComponentFixture<AnswerFlowPanel>, label: string): HTMLButtonElement {
  const buttons = Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
  const button = buttons.find((candidate) => candidate.textContent?.trim() === label);
  if (!button) {
    throw new Error(`Button not found: ${label}`);
  }
  return button;
}
