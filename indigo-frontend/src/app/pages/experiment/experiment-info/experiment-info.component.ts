import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CardComponent } from '@/core/components/common/card/card.component';
import { ExperimentService } from '@/core/services/experiment/experiment.service';
import { computed } from '@angular/core';

@Component({
  selector: 'eln-experiment-info',
  standalone: true,
  imports: [CommonModule, CardComponent],
  templateUrl: './experiment-info.component.html',
})
export class ExperimentInfoComponent {
  experimentService = inject(ExperimentService);
  
  // Computed signals from the service
  experiment = computed(() => this.experimentService.experiment());
  isLoading = computed(() => this.experimentService.isLoading());
  hasError = computed(() => this.experimentService.hasError());
}