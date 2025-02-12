import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('@core/components/layout/master.component').then(
        (c) => c.MasterComponent,
      ),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('@pages/projects/project-list.component').then(
            (c) => c.ProjectListComponent,
          ),
      },
    ],
  },
  {
    path: 'login',
    loadComponent: () =>
      import('@pages/account/login/login.component').then(
        (c) => c.LoginComponent,
      ),
  },
];
