import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { CanActivateFn, Router } from '@angular/router';
import { vi } from 'vitest';
import { AuthService } from './auth.service';
import { instructorGuard, studentGuard } from './role-guard';

describe('role guards', () => {
  const executeInstructorGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => instructorGuard(...guardParameters));

  const executeStudentGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => studentGuard(...guardParameters));

  const navigate = vi.fn();

  function configure(role: 'INSTRUCTOR' | 'STUDENT' | null) {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useValue: {
            role: signal(role).asReadonly(),
          },
        },
        {
          provide: Router,
          useValue: {
            navigate,
          },
        },
      ],
    });
  }

  beforeEach(() => {
    navigate.mockReset();
  });

  it('allows instructors into instructor routes', async () => {
    configure('INSTRUCTOR');

    const allowed = await executeInstructorGuard({} as never, {} as never);

    expect(allowed).toBe(true);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('redirects students away from instructor routes', async () => {
    configure('STUDENT');
    navigate.mockResolvedValue(true);

    const allowed = await executeInstructorGuard({} as never, {} as never);

    expect(allowed).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/student']);
  });

  it('allows students into student routes', async () => {
    configure('STUDENT');

    const allowed = await executeStudentGuard({} as never, {} as never);

    expect(allowed).toBe(true);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('redirects unknown roles to login', async () => {
    configure(null);
    navigate.mockResolvedValue(true);

    const allowed = await executeStudentGuard({} as never, {} as never);

    expect(allowed).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
