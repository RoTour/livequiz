import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AuthService } from '../login/auth.service';
import { StudentVerifyEmail } from './student-verify-email';

describe('StudentVerifyEmail', () => {
  let fixture: ComponentFixture<StudentVerifyEmail>;

  const verifyStudentEmail = vi.fn();
  const navigate = vi.fn();
  let queryParamMap = convertToParamMap({ token: 'verify-token' });

  beforeEach(async () => {
    verifyStudentEmail.mockReset();
    navigate.mockReset();
    queryParamMap = convertToParamMap({ token: 'verify-token' });

    await TestBed.configureTestingModule({
      imports: [StudentVerifyEmail],
      providers: [
        {
          provide: AuthService,
          useValue: {
            verifyStudentEmail,
          },
        },
        {
          provide: Router,
          useValue: {
            navigate,
          },
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              get queryParamMap() {
                return queryParamMap;
              },
            },
          },
        },
      ],
    }).compileComponents();
  });

  it('verifies email token and redirects to student lectures', async () => {
    verifyStudentEmail.mockReturnValue(of({ token: 'student-jwt' }));
    navigate.mockResolvedValue(true);

    fixture = TestBed.createComponent(StudentVerifyEmail);
    fixture.detectChanges();
    await fixture.whenStable();

    expect(verifyStudentEmail).toHaveBeenCalledWith('verify-token');
    expect(navigate).toHaveBeenCalledWith(['/student/lectures']);
  });

  it('shows explicit message when token is missing', async () => {
    queryParamMap = convertToParamMap({});

    fixture = TestBed.createComponent(StudentVerifyEmail);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(verifyStudentEmail).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).toContain('Missing verification token');
  });

  it('shows explicit message when token is expired', async () => {
    verifyStudentEmail.mockReturnValue(
      throwError(() => ({
        error: {
          code: 'EMAIL_VERIFICATION_TOKEN_EXPIRED',
        },
      })),
    );

    fixture = TestBed.createComponent(StudentVerifyEmail);
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Verification link expired');
    expect(fixture.nativeElement.textContent).toContain('has expired');
  });
});
