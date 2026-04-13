import { Component, computed, inject, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ProjectTabButtonComponent } from '@pages/project/project-tab-button/project-tab-button.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'eln-experiment-layout',
  templateUrl: './experiment-layout.component.html',
  standalone: true,
  imports: [
    RouterOutlet,
    ProjectTabButtonComponent,
    CommonModule,
    CardComponent,
    MatProgressSpinner,
  ],
})
export class ExperimentLayoutComponent implements OnInit, OnDestroy {
  private activatedRoute = inject(ActivatedRoute);
  experimentDetailService = inject(ExperimentDetailService);

  projectId = '';
  notebookId = '';
  experimentId = '';

  // Computed signals from the service
  experiment = computed<ExperimentDetail | null>(() =>
    this.experimentDetailService.experimentDetail(),
  );
  isLoading = computed<boolean>(() => this.experimentDetailService.isLoading());
  isUpdating = computed<boolean>(() =>
    this.experimentDetailService.isUpdating(),
  );
  hasError = computed<boolean>(() => this.experimentDetailService.hasError());

  // Tab URLs
  public infoUrl = '';
  public attachmentsUrl = '';
  public summaryUrl = '';
  public versionsUrl = '';

  ngOnInit(): void {
    // Get all route parameters
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
