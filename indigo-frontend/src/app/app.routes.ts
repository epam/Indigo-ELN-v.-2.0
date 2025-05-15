import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('@core/components/layout/master.component').then(
        (c) => c.MasterComponent,
      ),
    canActivate: [],
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
              import(
                '@pages/project/project-detail/project-detail.component'
              ).then((c) => c.ProjectDetailComponent),
            children: [
              {
                path: '',
                loadComponent: () =>
                  import(
                    '@pages/project/project-info/project-info.component'
                  ).then((c) => c.ProjectInfoComponent),
              },
              {
                path: 'notebooks',
                loadComponent: () =>
                  import(
                    '@/app/pages/project/notebook/notebook-list/notebook-list.component'
                  ).then((c) => c.NotebookListComponent),
              },
            ],
          },
        ],
      },
    ],
  },
];
