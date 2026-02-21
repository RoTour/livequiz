import { UserRole } from '../domain/user-role';

export function routeForRole(role: UserRole | null): string {
  if (role === 'INSTRUCTOR') {
    return '/instructor';
  }
  if (role === 'STUDENT') {
    return '/student/lectures';
  }
  return '/auth/login';
}
