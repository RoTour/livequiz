import { Routes } from '@angular/router';
import { Login } from './login/login';

export const routes: Routes = [
  {
    path: 'auth/login',
    component: Login,
  },
  { path: '', redirectTo: 'auth/login', pathMatch: 'full' },
];
