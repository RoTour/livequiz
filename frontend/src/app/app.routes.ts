import { Routes } from '@angular/router';
import { Login } from './login/login';
import { Dashboard } from './dashboard/dashboard';
import { authGuard } from './login/auth-guard';

export const routes: Routes = [
  {
    path: 'auth/login',
    component: Login,
  },
  {
    path: 'dashboard',
    component: Dashboard,
    canActivate: [authGuard],
  },
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
];
