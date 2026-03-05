import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { from, throwError } from 'rxjs';
import { vi } from 'vitest';

import { Login } from './login';
import { AuthService } from './auth.service';

describe('Login', () => {
  let component: Login;
  let fixture: ComponentFixture<Login>;
  const login = vi.fn();
  const requestStudentMagicLogin = vi.fn();
  const routeForCurrentUser = vi.fn();
  const logout = vi.fn();
  const navigate = vi.fn();
  const navigateByUrl = vi.fn();
  let queryParamMap = convertToParamMap({});

  beforeEach(async () => {
    login.mockReset();
    requestStudentMagicLogin.mockReset();
    routeForCurrentUser.mockReset();
    logout.mockReset();
    navigate.mockReset();
    navigateByUrl.mockReset();
    queryParamMap = convertToParamMap({});

    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        {
          provide: AuthService,
          useValue: {
            login,
            requestStudentMagicLogin,
            routeForCurrentUser,
            logout,
          },
        },
        {
          provide: Router,
          useValue: {
            navigate,
            navigateByUrl,
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

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('hides instructor login by default', () => {
    expect(component.showInstructorLogin()).toBe(false);
  });

  it('toggles instructor login visibility', () => {
    component.toggleInstructorLogin();
    expect(component.showInstructorLogin()).toBe(true);

    component.toggleInstructorLogin();
    expect(component.showInstructorLogin()).toBe(false);
  });

  it('clears instructor auth error when instructor panel is closed', () => {
    component.toggleInstructorLogin();
    component.authErrorMessage.set('Invalid instructor credentials.');

    component.closeInstructorLogin();

    expect(component.showInstructorLogin()).toBe(false);
    expect(component.authErrorMessage()).toBe('');
  });

  it('routes to the role default page after successful login', async () => {
    routeForCurrentUser.mockReturnValue('/student/lectures');
    login.mockReturnValue(from(Promise.resolve({ token: 'jwt' })));
    navigate.mockResolvedValue(true);
    component.form.setValue({ identifier: 'student@ynov.com', password: 'password' });

    component.submit();
    await fixture.whenStable();

    expect(login).toHaveBeenCalledWith('student@ynov.com', 'password');
    expect(routeForCurrentUser).toHaveBeenCalled();
    expect(navigate).toHaveBeenCalledWith(['/student/lectures']);
    expect(navigateByUrl).not.toHaveBeenCalled();
  });

  it('logs out and re-enables form when role target is unknown', async () => {
    routeForCurrentUser.mockReturnValue('/auth/login');
    login.mockReturnValue(from(Promise.resolve({ token: 'jwt' })));
    component.form.setValue({ identifier: 'user@ynov.com', password: 'password' });

    component.submit();
    await fixture.whenStable();

    expect(logout).toHaveBeenCalled();
    expect(navigate).not.toHaveBeenCalled();
    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(component.form.disabled).toBe(false);
    expect(component.authErrorMessage()).toContain('Could not determine your role');
  });

  it('re-enables form when post-login navigation fails', async () => {
    routeForCurrentUser.mockReturnValue('/student/lectures');
    login.mockReturnValue(from(Promise.resolve({ token: 'jwt' })));
    navigate.mockResolvedValue(false);
    component.form.setValue({ identifier: 'student@ynov.com', password: 'password' });

    component.submit();
    await fixture.whenStable();
    await Promise.resolve();

    expect(navigate).toHaveBeenCalledWith(['/student/lectures']);
    expect(navigateByUrl).not.toHaveBeenCalled();
    expect(component.form.disabled).toBe(false);
    expect(component.authErrorMessage()).toContain('Could not open your dashboard');
  });

  it('uses returnUrl when present after successful login', async () => {
    queryParamMap = convertToParamMap({ returnUrl: '/student/join/token-1' });
    routeForCurrentUser.mockReturnValue('/student/lectures');
    login.mockReturnValue(from(Promise.resolve({ token: 'jwt' })));
    navigateByUrl.mockResolvedValue(true);
    component.form.setValue({ identifier: 'student@ynov.com', password: 'password' });

    component.submit();
    await fixture.whenStable();

    expect(navigateByUrl).toHaveBeenCalledWith('/student/join/token-1');
    expect(navigate).not.toHaveBeenCalled();
  });

  it('re-enables form when login fails', () => {
    const consoleError = vi.spyOn(console, 'error').mockImplementation(() => undefined);
    login.mockReturnValue(throwError(() => new Error('invalid credentials')));
    component.form.setValue({ identifier: 'instructor@ynov.com', password: 'password' });

    component.submit();

    expect(component.form.disabled).toBe(false);
    consoleError.mockRestore();
  });

  it('requests student magic link and shows generic status message', async () => {
    requestStudentMagicLogin.mockReturnValue(from(Promise.resolve({ status: 'VERIFICATION_EMAIL_SENT_IF_ALLOWED' })));
    component.studentMagicLinkForm.setValue({ email: 'student@ynov.com' });

    component.requestStudentMagicLink();
    await fixture.whenStable();

    expect(requestStudentMagicLogin).toHaveBeenCalledWith('student@ynov.com');
    expect(component.studentMagicLinkStatusMessage()).toContain('If allowed, we sent you a student access link');
    expect(component.studentMagicLinkErrorMessage()).toBe('');
    expect(component.studentMagicLinkForm.disabled).toBe(false);
  });

  it('shows actionable error when student magic link request fails with domain policy error', async () => {
    requestStudentMagicLogin.mockReturnValue(
      throwError(() => ({ error: { code: 'EMAIL_DOMAIN_NOT_ALLOWED' } })),
    );
    component.studentMagicLinkForm.setValue({ email: 'student@gmail.com' });

    component.requestStudentMagicLink();
    await fixture.whenStable();

    expect(component.studentMagicLinkErrorMessage()).toBe('Only @ynov.com email addresses are allowed.');
    expect(component.studentMagicLinkStatusMessage()).toBe('');
    expect(component.studentMagicLinkForm.disabled).toBe(false);
  });
});
