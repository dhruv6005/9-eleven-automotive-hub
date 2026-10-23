import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/public/home.component').then((m) => m.HomeComponent),
  },
  {
    path: 'admin',
    loadComponent: () => import('./features/admin/admin-placeholder.component').then((m) => m.AdminPlaceholderComponent),
  },
  {
    path: 'portal',
    loadComponent: () => import('./features/customer/customer-placeholder.component').then((m) => m.CustomerPlaceholderComponent),
  },
  { path: '**', redirectTo: '' },
];
