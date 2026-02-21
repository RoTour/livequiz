import { routes } from './app.routes';
import { authGuard } from './login/auth-guard';
import { instructorGuard, studentGuard } from './login/role-guard';
import { InstructorHome } from './instructor/instructor-home';
import { StudentHome } from './student/student-home';
import { StudentJoinToken } from './student/student-join-token';

describe('app routes', () => {
  it('keeps legacy instructor path as guarded redirect', () => {
    const instructorLegacyRoute = routes.find((route) => route.path === 'instructor');

    expect(instructorLegacyRoute?.redirectTo).toBe('instructor/lectures');
    expect(instructorLegacyRoute?.pathMatch).toBe('full');
    expect(instructorLegacyRoute?.canActivate).toEqual([authGuard, instructorGuard]);
  });

  it('protects instructor lecture routes with auth and instructor guards', () => {
    const instructorListRoute = routes.find((route) => route.path === 'instructor/lectures');
    const instructorDetailRoute = routes.find((route) => route.path === 'instructor/lectures/:lectureId');

    expect(instructorListRoute?.component).toBe(InstructorHome);
    expect(instructorDetailRoute?.component).toBe(InstructorHome);
    expect(instructorListRoute?.canActivate).toEqual([authGuard, instructorGuard]);
    expect(instructorDetailRoute?.canActivate).toEqual([authGuard, instructorGuard]);
  });

  it('protects student routes with auth and student guards', () => {
    const studentRoute = routes.find((route) => route.path === 'student');
    const studentJoinRoute = routes.find((route) => route.path === 'student/join/:token');

    expect(studentRoute?.component).toBe(StudentHome);
    expect(studentJoinRoute?.component).toBe(StudentJoinToken);
    expect(studentRoute?.canActivate).toEqual([authGuard, studentGuard]);
    expect(studentJoinRoute?.canActivate).toEqual([authGuard, studentGuard]);
  });

  it('keeps fallback redirects for dashboard root and unknown paths', () => {
    const dashboardRoute = routes.find((route) => route.path === 'dashboard');
    const rootRoute = routes.find((route) => route.path === '');
    const wildcardRoute = routes.find((route) => route.path === '**');

    expect(dashboardRoute?.redirectTo).toBe('instructor');
    expect(rootRoute?.redirectTo).toBe('auth/login');
    expect(wildcardRoute?.redirectTo).toBe('auth/login');
  });
});
