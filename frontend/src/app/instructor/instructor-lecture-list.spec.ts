import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';
import { InstructorLectureList } from './instructor-lecture-list';
import { InstructorWorkspaceService } from './application/instructor-workspace.service';

describe('InstructorLectureList', () => {
  let component: InstructorLectureList;
  let fixture: ComponentFixture<InstructorLectureList>;

  const listLectures = vi.fn();
  const createLecture = vi.fn();

  beforeEach(async () => {
    listLectures.mockReset();
    createLecture.mockReset();
    listLectures.mockResolvedValue([]);

    await TestBed.configureTestingModule({
      imports: [InstructorLectureList],
      providers: [
        provideRouter([]),
        {
          provide: InstructorWorkspaceService,
          useValue: {
            listLectures,
            createLecture,
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(InstructorLectureList);
    component = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();
  });

  it('shows empty state when there are no lectures', () => {
    expect(listLectures).toHaveBeenCalledTimes(1);
    expect(fixture.nativeElement.textContent).toContain('No lectures yet. Start by creating one.');
  });

  it('renders lecture list returned by API', async () => {
    listLectures.mockResolvedValue([
      {
        lectureId: 'lecture-1',
        title: 'Distributed Systems',
        createdAt: '2026-02-21T11:00:00Z',
        questionCount: 3,
        unlockedCount: 2,
      },
    ]);

    await component.refreshLectures();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Distributed Systems');
    expect(fixture.nativeElement.textContent).toContain('3 questions, 2 unlocked');
  });

  it('creates lecture then navigates to lecture detail route', async () => {
    createLecture.mockResolvedValue('lecture-9');
    listLectures.mockResolvedValue([]);
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    component.createLectureForm.setValue({ title: 'DDD Workshop' });

    await component.createLecture();

    expect(createLecture).toHaveBeenCalledWith('DDD Workshop');
    expect(navigateSpy).toHaveBeenCalledWith(['/instructor/lectures', 'lecture-9']);
  });

  it('opens selected lecture from list', async () => {
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    await component.openLecture('lecture-12');

    expect(navigateSpy).toHaveBeenCalledWith(['/instructor/lectures', 'lecture-12']);
  });
});
