import { Routes } from '@angular/router';
import { Login } from './login/login';
import { authGuard } from './login/auth-guard';
import { instructorGuard, studentGuard } from './login/role-guard';
import { InstructorHome } from './instructor/instructor-home';
import { InstructorLectureList } from './instructor/instructor-lecture-list';
import { StudentHome } from './student/student-home';
import { StudentJoinToken } from './student/student-join-token';
import { DesignSystem } from './design-system/design-system';

export const routes: Routes = [
  {
    path: 'auth/login',
    component: Login,
  },
  {
    path: 'instructor',
    redirectTo: 'instructor/lectures',
    pathMatch: 'full',
  },
  {
    path: 'instructor/lectures',
    component: InstructorLectureList,
    canActivate: [authGuard, instructorGuard],
  },
  {
    path: 'instructor/lectures/:lectureId',
    component: InstructorHome,
    canActivate: [authGuard, instructorGuard],
  },
  {
    path: 'student',
    component: StudentHome,
    canActivate: [authGuard, studentGuard],
  },
  {
    path: 'design-system',
    component: DesignSystem,
  },
  {
    path: 'student/join/:token',
    component: StudentJoinToken,
    canActivate: [authGuard, studentGuard],
  },
  { path: 'dashboard', redirectTo: 'instructor', pathMatch: 'full' },
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: 'auth/login' },
];
