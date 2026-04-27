import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

import { BreadcrumbsComponent } from '@/core/components/breadcrumbs/breadcrumbs.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { BreadcrumbsStateService } from '@/core/services/breadcrumbs/breadcrumbs.state.service';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { ProjectTabButtonComponent } from '@pages/project/project-tab-button/project-tab-button.component';
import { UndoRedoDirective } from '@core/directives/undo-redo.directive';

@Component({
  selector: 'eln-experiment-layout',
  templateUrl: './experiment-layout.component.html',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    ProjectTabButtonComponent,
    CardComponent,
    MatProgressSpinner,
    UndoRedoDirective,
    BreadcrumbsComponent,
  ],
})
export class ExperimentLayoutComponent implements OnInit, OnDestroy {
  private activatedRoute = inject(ActivatedRoute);
  experimentDetailService = inject(ExperimentDetailService);
  breadcrumbsState = inject(BreadcrumbsStateService);

  projectId = '';
  notebookId = '';
  experimentId = '';

  // Computed signals from the service
  experiment = computed<ExperimentDetail | null>(() => this.experimentDetailService.experimentDetail());
  isLoading = computed<boolean>(() => this.experimentDetailService.isLoading());
  isUpdating = computed<boolean>(() => this.experimentDetailService.isUpdating());
  hasError = computed<boolean>(() => this.experimentDetailService.hasError());

  infoUrl = '';
  attachmentsUrl = '';
  summaryUrl = '';
  versionsUrl = '';

  private readonly breadcrumbsEffect = effect(() => {
    const experiment = this.experiment();

    if (!experiment || !this.projectId || !this.notebookId) {
      return;
    }

    this.breadcrumbsState.setItems([
      { label: 'All Projects', url: '/projects', active: false },
      {
        label: `Project: ${experiment.projectName}`,
        url: `/projects/${this.projectId}`,
        active: false,
      },
      {
        label: `Notebook: ${experiment.notebookName}`,
        url: `/projects/${this.projectId}/notebooks/${this.notebookId}`,
        active: false,
      },
      {
        label: `Experiment: ${experiment.name}`,
        active: true,
      },
    ]);
  });

  ngOnInit(): void {
    this.experimentId = this.activatedRoute.snapshot.params['experimentId'];
    this.notebookId = this.activatedRoute.snapshot.params['notebookId'];
    this.projectId = this.activatedRoute.snapshot.params['projectId'];

    if (this.experimentId) {
      // Set tab URLs using relative paths
      this.infoUrl = 'info';
      this.attachmentsUrl = 'attachments';
      this.summaryUrl = 'summary';
      this.versionsUrl = 'versions';
      this.experimentDetailService.load(this.experimentId);
    }
  }

  ngOnDestroy(): void {
    this.experimentDetailService.reset();
  }

  refreshData(): void {
    this.experimentDetailService.refresh();
  }
}
