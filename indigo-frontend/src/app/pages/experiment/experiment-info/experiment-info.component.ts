import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentImageService } from '@/core/services/experiment/experiment-image.service';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { SampleSearchComponent } from '@pages/experiment/sample-search/sample-search.component';
import { MatDialog } from '@angular/material/dialog';
import { ExperimentModelService } from '@core/services/experiment/experiment-model.service';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { ReactionViewComponent } from '@pages/experiment/stoichiometry/reaction-view/reaction-view.component';

@Component({
  selector: 'eln-experiment-info',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    MatProgressSpinner,
    CdkAccordionModule,
    ReactionViewComponent,
    ButtonComponent,
  ],
  providers: [ExperimentImageService],
  templateUrl: './experiment-info.component.html',
})
export class ExperimentInfoComponent implements OnInit {
  experimentDetailService = inject(ExperimentDetailService);
  experimentModelService = inject(ExperimentModelService);
  experimentImageService = inject(ExperimentImageService);

  dialog = inject(MatDialog);

  // Signal to track if model is being updated
  isUpdating = signal<boolean>(false);

  // Computed signals from the service
  experiment = computed(() => this.experimentDetailService.experimentDetail());
  isLoading = computed(() => this.experimentDetailService.isLoading());
  hasError = computed(() => this.experimentDetailService.hasError());

  // Computed signals from the model service
  model = computed(() => this.experimentModelService.experimentModel());
  modelLoading = computed(() => this.experimentModelService.isLoading());
  modelError = computed(() => this.experimentModelService.hasError());

  // Computed signals from image service
  experimentImageUrl = computed(() => this.experimentImageService.imageUrl());
  imageLoading = computed(
    () => this.experimentImageService.isLoading() || this.isUpdating(),
  );
  imageError = computed(() => this.experimentImageService.hasError());

  ngOnInit(): void {
    const experimentId = this.experiment()?.id;
    if (experimentId) {
      this.experimentImageService.load(experimentId);
      this.experimentModelService.load(experimentId);
    }
  }

  onModelUpdating(isUpdating: boolean): void {
    this.isUpdating.set(isUpdating);

    // When update completes, refresh the image
    if (!isUpdating) this.experimentImageService.refresh();
  }

  // TODO move to Stoichiometry table when it's available
  showAddMaterialDialog() {
    const [experiment, model] = [this.experiment(), this.model()];
    if (experiment && model) {
      this.dialog.open(SampleSearchComponent, {
        data: {
          experimentId: experiment.id,
          reactionAnchor: model.reactions[0].anchor,
        },
      });
    }
  }
}
