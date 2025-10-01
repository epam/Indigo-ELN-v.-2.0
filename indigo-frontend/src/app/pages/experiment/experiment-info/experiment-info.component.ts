import { Component, inject, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ExperimentDetailService } from '@/core/services/experiment/experiment-detail.service';
import { ReactionSchemeViewComponent } from '@/core/components/experiment/reaction-scheme-view/reaction-scheme-view.component';

@Component({
  selector: 'eln-experiment-info',
  standalone: true,
  imports: [CommonModule, CardComponent, ReactionSchemeViewComponent],
  templateUrl: './experiment-info.component.html',
})
export class ExperimentInfoComponent {
  experimentDetailService = inject(ExperimentDetailService);

  // Computed signals from the service
  experiment = computed(() => this.experimentDetailService.experimentDetail());
  isLoading = computed(() => this.experimentDetailService.isLoading());
  hasError = computed(() => this.experimentDetailService.hasError());
}