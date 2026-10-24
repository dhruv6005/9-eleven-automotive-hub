import { Routes } from '@angular/router';
import { authenticatedGuard, roleGuard } from './core/auth.guards';

const authPage = () => import('./features/auth/auth-page.component').then((m) => m.AuthPageComponent);

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/public/home.component').then((m) => m.HomeComponent) },
  { path: 'login', loadComponent: authPage },
  { path: 'register', loadComponent: authPage },
  { path: 'forgot-password', loadComponent: authPage },
  { path: 'reset-password', loadComponent: authPage },
  { path: 'verify-email', loadComponent: authPage },
  { path: 'change-password', loadComponent: authPage, canActivate: [authenticatedGuard] },
  {
    path: 'admin',
    canActivate: [authenticatedGuard, roleGuard],
    data: { role: 'ADMIN' },
    loadComponent: () => import('./features/admin/admin-shell.component').then((m) => m.AdminShellComponent),
  },
  {
    path: 'portal',
    canActivate: [authenticatedGuard, roleGuard],
    data: { role: 'CUSTOMER' },
    loadComponent: () => import('./features/customer/customer-shell.component').then((m) => m.CustomerShellComponent),
  },
  { path: '**', redirectTo: '' },
];
