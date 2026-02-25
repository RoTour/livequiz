import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { vi } from 'vitest';
import { StudentWorkspaceService } from './application/student-workspace.service';
import { StudentJoinToken } from './student-join-token';
import { AuthService } from '../login/auth.service';

describe('StudentJoinToken', () => {
  let fixture: ComponentFixture<StudentJoinToken>;

  const joinLectureByToken = vi.fn();
  const ensureStudentSession = vi.fn();
  const navigate = vi.fn();
  let role: 'INSTRUCTOR' | 'STUDENT' | null = null;

  beforeEach(async () => {
    joinLectureByToken.mockReset();
    ensureStudentSession.mockReset();
    navigate.mockReset();
    role = null;
    ensureStudentSession.mockResolvedValue(undefined);

    await TestBed.configureTestingModule({
      imports: [StudentJoinToken],
      providers: [
        {
          provide: StudentWorkspaceService,
          useValue: {
            joinLectureByToken,
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({ token: 'token-1' }),
            },
          },
        },
        {
          provide: Router,
          useValue: {
            navigate,
          },
        },
        {
          provide: AuthService,
          useValue: {
            ensureStudentSession,
            role: () => role,
          },
        },
      ],
    }).compileComponents();
  });

  async function initComponent() {
    fixture = TestBed.createComponent(StudentJoinToken);
    fixture.detectChanges();
    await fixture.whenStable();
    await fixture.whenStable();
    fixture.detectChanges();
  }

  it('auto-joins by token and redirects directly to lecture room route', async () => {
    joinLectureByToken.mockResolvedValue({
      lectureId: 'lecture-1',
      studentId: 'student-1',
      alreadyEnrolled: false,
      enrolledAt: '2026-02-20T10:00:00Z',
    });
    navigate.mockResolvedValue(true);

    await initComponent();

    expect(ensureStudentSession).toHaveBeenCalled();
    expect(joinLectureByToken).toHaveBeenCalledWith('token-1');
    expect(navigate).toHaveBeenCalledWith(['/student/lectures', 'lecture-1']);
  });

  it('redirects instructors away from student invite links', async () => {
    role = 'INSTRUCTOR';
    navigate.mockResolvedValue(true);

    await initComponent();

    expect(ensureStudentSession).not.toHaveBeenCalled();
    expect(joinLectureByToken).not.toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/instructor/lectures']);
  });

  it('shows actionable message when invite token is invalid', async () => {
    joinLectureByToken.mockRejectedValue({
      error: {
        code: 'INVITE_NOT_FOUND',
      },
    });

    await initComponent();

    expect(navigate).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Invite unavailable');
    expect(fixture.nativeElement.textContent).toContain('token is invalid');
  });

  it('shows explicit revoked invite message', async () => {
    joinLectureByToken.mockRejectedValue({
      error: {
        code: 'INVITE_REVOKED',
      },
    });

    await initComponent();

    expect(navigate).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Invite revoked');
    expect(fixture.nativeElement.textContent).toContain('has been revoked');
  });

  it('shows explicit expired invite message', async () => {
    joinLectureByToken.mockRejectedValue({
      error: {
        code: 'INVITE_EXPIRED',
      },
    });

    await initComponent();

    expect(navigate).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Invite expired');
    expect(fixture.nativeElement.textContent).toContain('has expired');
  });
});
