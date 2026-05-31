import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, OnDestroy, OnInit } from '@angular/core';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ActivatedRoute, RouterOutlet } from '@angular/router';

import { BreadcrumbsComponent } from '@/core/components/breadcrumbs/breadcrumbs.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { BreadcrumbsStateService } from '@/core/services/breadcrumbs/breadcrumbs.state.service';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { UndoRedoDirective } from '@core/directives/undo-redo.directive';
import { ProjectTabButtonComponent } from '@pages/project/project-tab-button/project-tab-button.component';

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

  experimentId = '';

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

    if (!experiment) {
      return;
    }

    this.breadcrumbsState.setItems([
      { label: 'All Projects', url: '/projects', active: false },
      {
        label: `Project: ${experiment.projectName}`,
        url: `/projects/${experiment.projectId}`,
        active: false,
      },
      {
        label: `Notebook: ${experiment.notebookName}`,
        url: `/notebooks/${experiment.notebookId}`,
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

    if (this.experimentId) {
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
