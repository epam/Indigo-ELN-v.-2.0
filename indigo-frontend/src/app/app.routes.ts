import { Routes } from '@angular/router';
import { RoleGuard } from './role.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('@core/components/layout/master.component').then((c) => c.MasterComponent),
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
          import('@pages/project/project-layout/project-layout.component').then((c) => c.ProjectLayoutComponent),
        children: [
          {
            path: '',
            loadComponent: () =>
              import('@pages/project/project-list/project-list.component').then((c) => c.ProjectListComponent),
          },
          {
            path: ':id',
            loadComponent: () =>
              import('@pages/project/project-detail/project-detail.component').then((c) => c.ProjectDetailComponent),
            children: [
              {
                path: '',
                loadComponent: () =>
                  import('@pages/project/project-info/project-info.component').then((c) => c.ProjectInfoComponent),
              },
              {
                path: 'notebooks',
                loadComponent: () =>
                  import('@pages/notebook/notebook-list/notebook-list.component').then((c) => c.NotebookListComponent),
              },
            ],
          },
        ],
      },
      {
        path: 'projects/:projectId/notebooks/:notebookId',
        loadComponent: () =>
          import('@/app/pages/notebook/notebook-detail/notebook-detail.component').then(
            (c) => c.NotebookDetailComponent,
          ),
        children: [
          {
            path: '',
            loadComponent: () =>
              import('@/app/pages/notebook/notebook-info/notebook-info.component').then((c) => c.NotebookInfoComponent),
          },
          {
            path: 'experiments',
            loadComponent: () =>
              import('@pages/notebook/notebook-experiments-tab/notebook-experiments-tab.component').then(
                (c) => c.NotebookExperimentsTabComponent,
              ),
          },
        ],
      },
      {
        path: 'dictionary',
        loadComponent: () =>
          import('@/app/pages/dictionary/dictionary-layout/dictionary-layout.component').then(
            (c) => c.DictionaryLayoutComponent,
          ),
        canActivate: [RoleGuard],
        data: { requiredPermission: 'MANAGE_DICTIONARIES' },
      },
      {
        path: 'projects/:projectId/notebooks/:notebookId/experiments/:experimentId',
        loadComponent: () =>
          import('@/app/pages/experiment/experiment-layout/experiment-layout.component').then(
            (c) => c.ExperimentLayoutComponent,
          ),
      },
      {
        path: 'templates',
        loadComponent: () =>
          import('@pages/template/template-layout/template-layout.component').then((c) => c.TemplateLayoutComponent),
      },
      {
        path: 'signatures',
        loadComponent: () =>
          import('@pages/signature/signature-layout/signature-layout.component').then(
            (c) => c.SignatureLayoutComponent,
          ),
        canActivate: [RoleGuard],
        data: { requiredPermission: 'SIGN_EXPERIMENTS' },
        children: [
          {
            path: '',
            loadComponent: () =>
              import('@pages/signature/signature-list/signature-list.component').then((c) => c.SignatureListComponent),
          },
        ],
      },
    ],
  },
];
