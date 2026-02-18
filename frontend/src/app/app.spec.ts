import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { signal } from '@angular/core';
import { vi } from 'vitest';
import { App } from './app';
import { AuthService } from './login/auth.service';
import { BackendStatusService } from './shared/backend-status/backend-status.service';
import { UserRole } from './auth/domain/user-role';

describe('App', () => {
  const authenticated = signal(false);
  const role = signal<UserRole | null>(null);
  const logout = vi.fn();

  beforeEach(async () => {
    authenticated.set(false);
    role.set(null);
    logout.mockReset();

    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: authenticated.asReadonly(),
            role: role.asReadonly(),
            logout,
            routeForCurrentUser: () => (role() === 'INSTRUCTOR' ? '/instructor' : '/student'),
          },
        },
        {
          provide: BackendStatusService,
          useValue: {
            backendUp: signal(true).asReadonly(),
            lastChange: signal(new Date()).asReadonly(),
          },
        },
      ],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(App);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should render router outlet', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('router-outlet')).toBeTruthy();
  });

  it('shows role navigation when authenticated', () => {
    authenticated.set(true);
    role.set('INSTRUCTOR');
    const fixture = TestBed.createComponent(App);

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).toContain('INSTRUCTOR');
    expect(compiled.textContent).toContain('Instructor');
    expect(compiled.textContent).toContain('Log out');
  });

  it('hides navigation when unauthenticated', () => {
    const fixture = TestBed.createComponent(App);

    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    expect(compiled.textContent).not.toContain('Log out');
  });

  it('logs out and redirects to login', async () => {
    authenticated.set(true);
    role.set('STUDENT');
    const fixture = TestBed.createComponent(App);
    const router = TestBed.inject(Router);
    const navigateSpy = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    fixture.detectChanges();
    const logoutButton = fixture.nativeElement.querySelector('button') as HTMLButtonElement;
    logoutButton.click();
    await fixture.whenStable();

    expect(logout).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['/auth/login']);
  });
});
