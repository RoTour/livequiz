import { routes } from './app.routes';
import { authGuard } from './login/auth-guard';
import { instructorGuard, studentGuard } from './login/role-guard';
import { InstructorHome } from './instructor/instructor-home';
import { StudentHome } from './student/student-home';
import { StudentJoinToken } from './student/student-join-token';

describe('app routes', () => {
  it('protects instructor route with auth and instructor guards', () => {
    const instructorRoute = routes.find((route) => route.path === 'instructor');

    expect(instructorRoute?.component).toBe(InstructorHome);
    expect(instructorRoute?.canActivate).toEqual([authGuard, instructorGuard]);
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
