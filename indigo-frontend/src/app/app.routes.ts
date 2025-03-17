import { AuthGuard } from '@/core/guards/auth.guard';
import { GuestGuard } from '@/core/guards/guest.guard';
import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('@core/components/layout/master.component').then(
        (c) => c.MasterComponent,
      ),
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        redirectTo: 'projects',
        pathMatch: 'full',
      },
      {
        path: 'projects',
        loadComponent: () =>
          import('@pages/project/project-layout/project-layout.component').then(
            (c) => c.ProjectLayoutComponent,
          ),
        children: [
          {
            path: '',
            loadComponent: () =>
              import('@pages/project/project-list/project-list.component').then(
                (c) => c.ProjectListComponent,
              ),
          },
          {
            path: ':id',
            loadComponent: () =>
              import('@pages/project/project-info/project-info.component').then(
                (c) => c.ProjectInfoComponent,
              ),
            data: {
              title: 'Project',
            },
          },
        ],
      },
    ],
  },
  {
    path: 'login',
    loadComponent: () =>
      import('@pages/account/login/login.component').then(
        (c) => c.LoginComponent,
      ),
    canActivate: [GuestGuard],
  },
];
