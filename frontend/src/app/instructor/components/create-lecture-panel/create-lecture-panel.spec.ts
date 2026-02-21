import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { vi } from 'vitest';
import { CreateLecturePanel } from './create-lecture-panel';

describe('CreateLecturePanel', () => {
  let fixture: ComponentFixture<CreateLecturePanel>;
  let form: FormGroup;

  beforeEach(async () => {
    form = new FormGroup({
      title: new FormControl('', [Validators.required, Validators.minLength(3)]),
    });

    await TestBed.configureTestingModule({
      imports: [CreateLecturePanel],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateLecturePanel);
    fixture.componentRef.setInput('form', form);
    fixture.componentRef.setInput('selectedLectureId', '');
    fixture.detectChanges();
  });

  it('emits create event when submitted with valid form', () => {
    const createLecture = vi.fn();
    fixture.componentInstance.createLecture.subscribe(createLecture);
    form.setValue({ title: 'Domain Design' });
    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement;
    submitButton.click();

    expect(createLecture).toHaveBeenCalledTimes(1);
  });

  it('shows selected lecture id and disables submit when form is invalid', () => {
    fixture.componentRef.setInput('selectedLectureId', 'lecture-42');
    fixture.detectChanges();

    const submitButton = fixture.nativeElement.querySelector('button[type="submit"]') as HTMLButtonElement;
    expect(submitButton.disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('lecture-42');
  });
});
