import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { vi } from 'vitest';
import { JoinLecturePanel } from './join-lecture-panel';

describe('JoinLecturePanel', () => {
  let fixture: ComponentFixture<JoinLecturePanel>;
  let joinLectureForm: FormGroup;

  beforeEach(async () => {
    joinLectureForm = new FormGroup({
      code: new FormControl('', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]),
    });

    await TestBed.configureTestingModule({
      imports: [JoinLecturePanel],
    }).compileComponents();

    fixture = TestBed.createComponent(JoinLecturePanel);
    fixture.componentRef.setInput('joinLectureForm', joinLectureForm);
    fixture.componentRef.setInput('selectedLectureId', '');
    fixture.componentRef.setInput('joinResult', '');
    fixture.detectChanges();
  });

  it('emits join action when submitting a valid invite code', () => {
    const joinLecture = vi.fn();
    fixture.componentInstance.joinLecture.subscribe(joinLecture);
    joinLectureForm.setValue({ code: 'ABC123' });
    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement;
    submitButton.click();

    expect(joinLecture).toHaveBeenCalledTimes(1);
  });

  it('shows selected lecture and join outcome details', () => {
    fixture.componentRef.setInput('selectedLectureId', 'lecture-7');
    fixture.componentRef.setInput('joinResult', 'Already enrolled');
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('lecture-7');
    expect(fixture.nativeElement.textContent).toContain('Already enrolled');
  });
});
