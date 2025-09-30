import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ProjectTabButtonComponent } from '@/core/components/project/project-tab-button/project-tab-button.component';

import { ExperimentService } from '@/core/services/experiment/experiment.service';
import { ExperimentDetail } from '@/core/types/entities/experiment-detail.i';
import { computed } from '@angular/core';
import { CardComponent } from '@/core/components/common/card/card.component';

@Component({
  selector: 'eln-experiment-detail',
  templateUrl: './experiment-detail.component.html',
  standalone: true,
  imports: [
    RouterOutlet,
    ProjectTabButtonComponent,
    CommonModule,
    CardComponent,
  ],
  providers: [ExperimentService],
})
export class ExperimentDetailComponent implements OnInit, OnDestroy {
  private activatedRoute = inject(ActivatedRoute);
  experimentService = inject(ExperimentService);

  projectId = '';
  notebookId = '';
  experimentId = '';

  // Computed signals from the service
  experiment = computed<ExperimentDetail | null>(() =>
    this.experimentService.experiment(),
  );
  isLoading = computed<boolean>(() => this.experimentService.isLoading());
  hasError = computed<boolean>(() => this.experimentService.hasError());

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
      this.experimentService.load(this.experimentId);

      // Set tab URLs using relative paths
      this.infoUrl = 'info';
      this.attachmentsUrl = 'attachments';
      this.summaryUrl = 'summary';
      this.versionsUrl = 'versions';
    }
  }

  ngOnDestroy(): void {
    this.experimentService.reset();
  }

  refreshData(): void {
    this.experimentService.refresh();
  }
}
