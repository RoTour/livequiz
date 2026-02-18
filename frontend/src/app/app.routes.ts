import { Routes } from '@angular/router';
import { Login } from './login/login';
import { authGuard } from './login/auth-guard';
import { instructorGuard, studentGuard } from './login/role-guard';
import { InstructorHome } from './instructor/instructor-home';
import { StudentHome } from './student/student-home';
import { StudentJoinToken } from './student/student-join-token';

export const routes: Routes = [
  {
    path: 'auth/login',
    component: Login,
  },
  {
    path: 'instructor',
    component: InstructorHome,
    canActivate: [authGuard, instructorGuard],
  },
  {
    path: 'student',
    component: StudentHome,
    canActivate: [authGuard, studentGuard],
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
