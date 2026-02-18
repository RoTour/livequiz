import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { routeForRole } from '../auth/application/role-routing';
import { AuthService } from './auth.service';

export const instructorGuard: CanActivateFn = async () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.role() === 'INSTRUCTOR') {
    return true;
  }

  await router.navigate([routeForRole(authService.role())]);
  return false;
};

export const studentGuard: CanActivateFn = async () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.role() === 'STUDENT') {
    return true;
  }

  await router.navigate([routeForRole(authService.role())]);
  return false;
};
