import {Routes} from '@angular/router';
import {authGuard, guestGuard} from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./shared/layouts/shell-layout/shell-layout.component').then((m) => m.ShellLayoutComponent),
    children: [
      {path: '', pathMatch: 'full', redirectTo: 'tasks'},
      {
        path: 'login',
        canActivate: [guestGuard],
        loadComponent: () =>
          import('./features/auth/login/login.component').then((m) => m.LoginComponent),
      },
      {
        path: 'register',
        canActivate: [guestGuard],
        loadComponent: () =>
          import('./features/auth/register/register.component').then((m) => m.RegisterComponent),
      },
      {
        path: 'tasks',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/tasks/tasks-page/tasks-page.component').then((m) => m.TasksPageComponent),
      },
      {
        path: 'profile',
        canActivate: [authGuard],
        loadComponent: () =>
          import('./features/auth/profile/profile.component').then((m) => m.ProfileComponent),
      },
      {path: '**', redirectTo: 'tasks'},
    ],
  },
];
