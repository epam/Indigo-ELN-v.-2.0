import { CommonModule } from '@angular/common';
import { Component, computed, effect, inject, OnDestroy, OnInit } from '@angular/core';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { ActivatedRoute } from '@angular/router';

import { BreadcrumbsComponent } from '@/core/components/breadcrumbs/breadcrumbs.component';
import { CardComponent } from '@/core/components/common/card/card.component';
import { BreadcrumbsStateService } from '@/core/services/breadcrumbs/breadcrumbs.state.service';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentDetail } from '@/core/types/entities/experiments/experiment-detail.i';
import { UndoRedoDirective } from '@core/directives/undo-redo.directive';
import { TwsxPipe } from '@core/pipes/twsx.pipe';
import { TemplateTab } from '@core/types/entities/template.i';
import { ExperimentActionsComponent } from '@pages/experiment/experiment-actions/experiment-actions.component';
import { ExperimentAttachmentsComponent } from '@pages/experiment/experiment-attachments/experiment-attachments.component';
import { ExperimentComponentWrapperComponent } from '@pages/experiment/experiment-component-wrapper/experiment-component-wrapper.component';
import { ExperimentDescriptionComponent } from '@pages/experiment/experiment-description/experiment-description.component';
import { ExperimentDetailsComponent } from '@pages/experiment/experiment-details/experiment-details.component';
import { ExperimentVersionsComponent } from '@pages/experiment/experiment-versions/experiment-versions.component';
import { ProductBatchSummaryTableComponent } from '@pages/experiment/stoichiometry/product-batch-summary-table/product-batch-summary-table.component';
import { ReactionViewComponent } from '@pages/experiment/stoichiometry/reaction-view/reaction-view.component';

interface Tab {
  data: TemplateTab;
  created: boolean;
  selected: boolean;
}

@Component({
  selector: 'eln-experiment-layout',
  templateUrl: './experiment-layout.component.html',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatProgressSpinner,
    UndoRedoDirective,
    BreadcrumbsComponent,
    ExperimentActionsComponent,
    TwsxPipe,
    ExperimentAttachmentsComponent,
    ProductBatchSummaryTableComponent,
    ExperimentDetailsComponent,
    ExperimentDescriptionComponent,
    ExperimentVersionsComponent,
    ExperimentComponentWrapperComponent,
    ReactionViewComponent,
  ],
})
export class ExperimentLayoutComponent implements OnInit, OnDestroy {
  activatedRoute = inject(ActivatedRoute);
  experimentDetailService = inject(ExperimentDetailService);
  breadcrumbsState = inject(BreadcrumbsStateService);

  experimentId = '';

  experiment = computed<ExperimentDetail | null>(() => this.experimentDetailService.experimentDetail());
  template = computed(() => this.experimentDetailService.experimentTemplate());
  isLoading = computed<boolean>(() => this.experimentDetailService.isLoading());
  isUpdating = computed<boolean>(() => this.experimentDetailService.isUpdating());
  hasError = computed<boolean>(() => this.experimentDetailService.hasError());

  tabs: Tab[] | null = null;

  private readonly breadcrumbsEffect = effect(() => {
    const experiment = this.experiment();

    if (!experiment) {
      return;
    }

    this.breadcrumbsState.setItems([
      { label: 'All Projects', url: '/projects', active: false },
      {
        label: `Project: ${experiment.projectName ?? ''}`,
        url: `/projects/${experiment.projectId}`,
        active: false,
      },
      {
        label: `Notebook: ${experiment.notebookName ?? ''}`,
        url: `/notebooks/${experiment.notebookId}`,
        active: false,
      },
      {
        label: `Experiment: ${experiment.name ?? ''}`,
        active: true,
      },
    ]);
  });

  private readonly tabsEffect = effect(() => {
    const template = this.template();
    if (!template) {
      this.tabs = null;
    } else {
      this.tabs = template.templateTabs.map((t) => ({ data: t, created: false, selected: false }) as Tab);
      this.tabs[0].selected = true;
      this.tabs[0].created = true;
    }
  });

  ngOnInit(): void {
    const experimentId = this.activatedRoute.snapshot.params['experimentId'];
    this.experimentDetailService.load(experimentId);
  }

  ngOnDestroy(): void {
    this.experimentDetailService.reset();
  }

  selectTab(tab: Tab) {
    this.tabs.forEach((tab) => (tab.selected = false));
    tab.selected = true;
    tab.created = true;
  }

  refreshData(): void {
    this.experimentDetailService.refresh();
  }
}
