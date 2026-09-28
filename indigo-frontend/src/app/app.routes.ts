import { Routes } from '@angular/router';
import { ApplicationPermission } from '@/core/types/entities/user.i';
import { roleGuard } from './role.guard';

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
        data: { title: 'IndigoELN - Projects' },
        children: [
          {
            path: '',
            loadComponent: () =>
              import('@pages/project/project-list/project-list.component').then((c) => c.ProjectListComponent),
            data: { title: 'IndigoELN - Projects' },
          },
          {
            path: ':projectId',
            loadComponent: () =>
              import('@pages/project/project-detail/project-detail.component').then((c) => c.ProjectDetailComponent),
            data: { title: 'IndigoELN - Project' },
            children: [
              {
                path: '',
                loadComponent: () =>
                  import('@pages/project/project-info/project-info.component').then((c) => c.ProjectInfoComponent),
                data: { title: 'IndigoELN - Project' },
              },
              {
                path: 'notebooks',
                loadComponent: () =>
                  import('@pages/notebook/notebook-list/notebook-list.component').then((c) => c.NotebookListComponent),
                data: { title: 'IndigoELN - Project Notebooks' },
              },
            ],
          },
        ],
      },
      {
        path: 'notebooks/:notebookId',
        loadComponent: () =>
          import('@/app/pages/notebook/notebook-detail/notebook-detail.component').then(
            (c) => c.NotebookDetailComponent,
          ),
        data: { title: 'IndigoELN - Notebook' },
        children: [
          {
            path: '',
            loadComponent: () =>
              import('@/app/pages/notebook/notebook-info/notebook-info.component').then((c) => c.NotebookInfoComponent),
            data: { title: 'IndigoELN - Notebook' },
          },
          {
            path: 'experiments',
            loadComponent: () =>
              import('@pages/notebook/notebook-experiments-tab/notebook-experiments-tab.component').then(
                (c) => c.NotebookExperimentsTabComponent,
              ),
            data: { title: 'IndigoELN - Notebook Experiments' },
          },
        ],
      },
      {
        path: 'dictionary',
        loadComponent: () =>
          import('@/app/pages/dictionary/dictionary-layout/dictionary-layout.component').then(
            (c) => c.DictionaryLayoutComponent,
          ),
        canActivate: [roleGuard],
        data: { requiredPermission: ApplicationPermission.MANAGE_DICTIONARIES },
      },
      {
        path: 'experiments/:experimentId',
        loadComponent: () =>
          import('@/app/pages/experiment/experiment-layout/experiment-layout.component').then(
            (c) => c.ExperimentLayoutComponent,
          ),
      },
      // TODO templates are hidden until editing is implemented
      // {
      //   path: 'templates',
      //   loadComponent: () =>
      //     import('@pages/template/template-layout/template-layout.component').then((c) => c.TemplateLayoutComponent),
      //   canActivate: [roleGuard],
      //   data: { requiredPermission: ApplicationPermission.MANAGE_TEMPLATES },
      // },
      {
        path: 'signatures',
        loadComponent: () =>
          import('@pages/signature/signature-layout/signature-layout.component').then(
            (c) => c.SignatureLayoutComponent,
          ),
        canActivate: [roleGuard],
        data: { requiredPermission: ApplicationPermission.SIGN_EXPERIMENTS },
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
