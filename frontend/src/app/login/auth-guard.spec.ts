import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';
import { signal } from '@angular/core';
import { vi } from 'vitest';

import { authGuard } from './auth-guard';
import { AuthService } from './auth.service';
import { Router } from '@angular/router';

describe('authGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => authGuard(...guardParameters));

  const navigate = vi.fn();

  function configure(authenticated: boolean) {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useValue: {
            isAuthenticated: signal(authenticated).asReadonly(),
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

  it('allows navigation when authenticated', async () => {
    configure(true);

    const allowed = await executeGuard({} as never, {} as never);

    expect(allowed).toBe(true);
    expect(navigate).not.toHaveBeenCalled();
  });

  it('redirects to login when unauthenticated', async () => {
    configure(false);
    navigate.mockResolvedValue(true);

    const allowed = await executeGuard({} as never, {} as never);

    expect(allowed).toBe(false);
    expect(navigate).toHaveBeenCalledWith(['/auth/login']);
  });
});
