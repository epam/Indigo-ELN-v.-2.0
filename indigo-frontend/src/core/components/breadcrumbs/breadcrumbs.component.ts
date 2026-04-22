import { CommonModule } from '@angular/common';
import {
  Component,
  DestroyRef,
  OnInit,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ActivatedRoute,
  NavigationEnd,
  Router,
  RouterLink,
} from '@angular/router';
import { filter } from 'rxjs';

import { BreadcrumbService } from '../../services/breadcrumbs/breadcrumbs.service';
import {
  BREADCRUMB_LABELS,
  BreadcrumbItem,
  BreadcrumbType,
  RouteParams,
} from './breadcrumbs.shared';

@Component({
  selector: 'eln-breadcrumbs',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './breadcrumbs.component.html',
  styleUrl: './breadcrumbs.component.scss',
})
export class BreadcrumbsComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly breadcrumbService = inject(BreadcrumbService);
  private readonly destroyRef = inject(DestroyRef);

  private breadcrumbRequestId = 0;

  items: BreadcrumbItem[] = [];

  ngOnInit(): void {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(() => {
        this.buildBreadcrumbs();
      });

    this.buildBreadcrumbs();
  }

  private buildBreadcrumbs(): void {
    const requestId = ++this.breadcrumbRequestId;

    if (!this.router.url.startsWith('/projects')) {
      this.items = [];
      console.log('[Breadcrumbs] items:', this.items);
      return;
    }

    const params = this.collectRouteParams(this.activatedRoute);
    const { projectId, notebookId, experimentId } = params;
    const breadcrumbType = this.getBreadcrumbType(params);

    switch (breadcrumbType) {
      case BreadcrumbType.Root:
        this.items = [this.activeItem(BREADCRUMB_LABELS.allProjects)];
        console.log('[Breadcrumbs] items:', this.items);
        return;

      case BreadcrumbType.Project:
        if (projectId) {
          this.loadProjectBreadcrumb(projectId, requestId);
        }
        return;

      case BreadcrumbType.Notebook:
        if (projectId && notebookId) {
          this.loadNotebookBreadcrumb(projectId, notebookId, requestId);
        }
        return;

      case BreadcrumbType.Experiment:
        if (projectId && notebookId && experimentId) {
          this.loadExperimentBreadcrumb(
            projectId,
            notebookId,
            experimentId,
            requestId,
          );
        }
        return;
    }
  }

  private loadProjectBreadcrumb(
    projectId: string,
    requestId: number,
  ): void {
    this.breadcrumbService
      .getProject(projectId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (project) => {
          if (requestId !== this.breadcrumbRequestId) {
            return;
          }

          this.items = [
            this.allProjectsItem(),
            this.activeItem(`${BREADCRUMB_LABELS.project}: ${project.name}`),
          ];

          console.log('[Breadcrumbs] items:', this.items);
        },
        error: () => {
          if (requestId !== this.breadcrumbRequestId) {
            return;
          }

          this.items = [
            this.allProjectsItem(),
            this.activeItem(BREADCRUMB_LABELS.project),
          ];

          console.log('[Breadcrumbs] items:', this.items);
        },
      });
  }

  private loadNotebookBreadcrumb(
    projectId: string,
    notebookId: string,
    requestId: number,
  ): void {
    this.breadcrumbService
      .getNotebookData(projectId, notebookId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ project, notebook }) => {
          if (requestId !== this.breadcrumbRequestId) {
            return;
          }

          this.items = [
            ...this.projectTrail(projectId, project.name),
            this.activeItem(`${BREADCRUMB_LABELS.notebook}: ${notebook.name}`),
          ];

          console.log('[Breadcrumbs] items:', this.items);
        },
        error: () => {
          if (requestId !== this.breadcrumbRequestId) {
            return;
          }

          this.items = [
            ...this.projectFallbackTrail(projectId),
            this.activeItem(BREADCRUMB_LABELS.notebook),
          ];

          console.log('[Breadcrumbs] items:', this.items);
        },
      });
  }

  private loadExperimentBreadcrumb(
    projectId: string,
    notebookId: string,
    experimentId: string,
    requestId: number,
  ): void {
    this.breadcrumbService
      .getExperimentData(projectId, notebookId, experimentId)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ project, notebook, experiment }) => {
          if (requestId !== this.breadcrumbRequestId) {
            return;
          }

          this.items = [
            ...this.projectTrail(projectId, project.name),
            this.linkItem(
              `${BREADCRUMB_LABELS.notebook}: ${notebook.name}`,
              `/projects/${projectId}/notebooks/${notebookId}`,
            ),
            this.activeItem(
              `${BREADCRUMB_LABELS.experiment}: ${experiment.name}`,
            ),
          ];

          console.log('[Breadcrumbs] items:', this.items);
        },
        error: () => {
          if (requestId !== this.breadcrumbRequestId) {
            return;
          }

          this.items = [
            ...this.projectFallbackTrail(projectId),
            this.linkItem(
              BREADCRUMB_LABELS.notebook,
              `/projects/${projectId}/notebooks/${notebookId}`,
            ),
            this.activeItem(BREADCRUMB_LABELS.experiment),
          ];

          console.log('[Breadcrumbs] items:', this.items);
        },
      });
  }

  private getBreadcrumbType(params: RouteParams): BreadcrumbType {
    if (!params.projectId) {
      return BreadcrumbType.Root;
    }

    if (params.notebookId && params.experimentId) {
      return BreadcrumbType.Experiment;
    }

    if (params.notebookId) {
      return BreadcrumbType.Notebook;
    }

    return BreadcrumbType.Project;
  }

  private collectRouteParams(route: ActivatedRoute): RouteParams {
    const params: RouteParams = {};

    for (
      let currentRoute: ActivatedRoute | null = route;
      currentRoute;
      currentRoute = currentRoute.firstChild
    ) {
      const snapshotParams = currentRoute.snapshot.params;

      params.projectId ??=
        snapshotParams['projectId'] ?? snapshotParams['id'];
      params.notebookId ??= snapshotParams['notebookId'];
      params.experimentId ??= snapshotParams['experimentId'];
    }

    return params;
  }

  private allProjectsItem(): BreadcrumbItem {
    return this.linkItem(BREADCRUMB_LABELS.allProjects, '/projects');
  }

  private projectTrail(
    projectId: string,
    projectName: string,
  ): BreadcrumbItem[] {
    return [
      this.allProjectsItem(),
      this.linkItem(
        `${BREADCRUMB_LABELS.project}: ${projectName}`,
        `/projects/${projectId}`,
      ),
    ];
  }

  private projectFallbackTrail(projectId: string): BreadcrumbItem[] {
    return [
      this.allProjectsItem(),
      this.linkItem(BREADCRUMB_LABELS.project, `/projects/${projectId}`),
    ];
  }

  private linkItem(label: string, url: string): BreadcrumbItem {
    return { label, url, active: false };
  }

  private activeItem(label: string): BreadcrumbItem {
    return { label, active: true };
  }
}