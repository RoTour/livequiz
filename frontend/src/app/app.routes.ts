import { Routes } from '@angular/router';
import { Login } from './login/login';
import { authGuard } from './login/auth-guard';
import { instructorGuard, studentGuard } from './login/role-guard';
import { InstructorHome } from './instructor/instructor-home';
import { InstructorInviteQr } from './instructor/instructor-invite-qr';
import { InstructorLectureList } from './instructor/instructor-lecture-list';
import { StudentLectureList } from './student/student-lecture-list';
import { StudentLectureRoom } from './student/student-lecture-room';
import { StudentJoinToken } from './student/student-join-token';
import { StudentVerifyEmail } from './student/student-verify-email';
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
    path: 'instructor/invites/:inviteId/qr',
    component: InstructorInviteQr,
    canActivate: [authGuard, instructorGuard],
  },
  {
    path: 'student',
    redirectTo: 'student/lectures',
    pathMatch: 'full',
  },
  {
    path: 'student/lectures',
    component: StudentLectureList,
    canActivate: [authGuard, studentGuard],
  },
  {
    path: 'student/lectures/:lectureId',
    component: StudentLectureRoom,
    canActivate: [authGuard, studentGuard],
  },
  {
    path: 'design-system',
    component: DesignSystem,
  },
  {
    path: 'student/join/:token',
    component: StudentJoinToken,
  },
  {
    path: 'student/verify-email',
    component: StudentVerifyEmail,
  },
  { path: 'dashboard', redirectTo: 'instructor', pathMatch: 'full' },
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: '**', redirectTo: 'auth/login' },
];
