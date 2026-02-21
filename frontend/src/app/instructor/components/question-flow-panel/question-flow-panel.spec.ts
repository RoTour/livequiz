import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { vi } from 'vitest';
import { QuestionFlowPanel } from './question-flow-panel';

describe('QuestionFlowPanel', () => {
  let fixture: ComponentFixture<QuestionFlowPanel>;
  let form: FormGroup;

  beforeEach(async () => {
    form = new FormGroup({
      prompt: new FormControl('', [Validators.required, Validators.minLength(5)]),
      modelAnswer: new FormControl('', [Validators.required, Validators.minLength(3)]),
      timeLimitSeconds: new FormControl(60, [Validators.required, Validators.min(10)]),
    });

    await TestBed.configureTestingModule({
      imports: [QuestionFlowPanel],
    }).compileComponents();

    fixture = TestBed.createComponent(QuestionFlowPanel);
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('selectedLectureId', 'lecture-1');
    fixture.detectChanges();
  });

  it('emits add question on submit when form is valid', () => {
    const addQuestion = vi.fn();
    fixture.componentInstance.addQuestion.subscribe(addQuestion);
    form.setValue({
      prompt: 'What is an aggregate root?',
      modelAnswer: 'Consistency boundary owner',
      timeLimitSeconds: 60,
    });
    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement;
    submitButton.click();

    expect(addQuestion).toHaveBeenCalledTimes(1);
  });

  it('emits unlock and refresh actions and disables controls without selected lecture', () => {
    const unlockNext = vi.fn();
    const refreshState = vi.fn();
    fixture.componentInstance.unlockNext.subscribe(unlockNext);
    fixture.componentInstance.refreshState.subscribe(refreshState);

    const unlockButton = findButton(fixture, 'Unlock next');
    const refreshButton = findButton(fixture, 'Refresh state');
    unlockButton.click();
    refreshButton.click();

    expect(unlockNext).toHaveBeenCalledTimes(1);
    expect(refreshState).toHaveBeenCalledTimes(1);

    fixture.componentRef.setInput('selectedLectureId', '');
    fixture.detectChanges();

    expect((fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement).disabled).toBe(true);
    expect(findButton(fixture, 'Unlock next').disabled).toBe(true);
    expect(findButton(fixture, 'Refresh state').disabled).toBe(true);
  });
});

function findButton(fixture: ComponentFixture<QuestionFlowPanel>, label: string): HTMLButtonElement {
  const buttons = Array.from(fixture.nativeElement.querySelectorAll('button')) as HTMLButtonElement[];
  const button = buttons.find((candidate) => candidate.textContent?.trim() === label);
  if (!button) {
    throw new Error(`Button not found: ${label}`);
  }
  return button;
}
