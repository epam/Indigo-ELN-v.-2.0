import { Component, inject, computed, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ExperimentImageService } from '@/core/services/experiment/experiment-image.service';
import { ReactionSchemeViewComponent } from '@/core/components/experiment/reaction-scheme-view/reaction-scheme-view.component';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { CdkAccordionModule } from '@angular/cdk/accordion';

@Component({
  selector: 'eln-experiment-info',
  standalone: true,
  imports: [
    CommonModule,
    CardComponent,
    ReactionSchemeViewComponent,
    MatProgressSpinner,
    CdkAccordionModule,
  ],
  providers: [ExperimentImageService],
  templateUrl: './experiment-info.component.html',
})
export class ExperimentInfoComponent implements OnInit {
  experimentDetailService = inject(ExperimentDetailService);
  experimentImageService = inject(ExperimentImageService);

  // Computed signals from the service
  experiment = computed(() => this.experimentDetailService.experimentDetail());
  isLoading = computed(() => this.experimentDetailService.isLoading());
  hasError = computed(() => this.experimentDetailService.hasError());

  // Computed signals from image service
  experimentImageUrl = computed(() => this.experimentImageService.imageUrl());
  imageLoading = computed(() => this.experimentImageService.isLoading());
  imageError = computed(() => this.experimentImageService.hasError());

  ngOnInit(): void {
    const experimentId = this.experiment()?.id;
    if (experimentId) {
      this.experimentImageService.load(experimentId);
    }
  }
}