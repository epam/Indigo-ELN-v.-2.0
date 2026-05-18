import { Component, computed, inject } from '@angular/core';
import { ExperimentDetailsComponent } from '@pages/experiment/experiment-details/experiment-details.component';
import { ExperimentDescriptionComponent } from '@pages/experiment/experiment-description/experiment-description.component';
import { StoichiometryTableComponent } from '@pages/experiment/stoichiometry-table/stoichiometry-table.component';
import { ExperimentAttachmentsComponent } from '@pages/experiment/experiment-attachments/experiment-attachments.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';

@Component({
  selector: 'eln-experiment-info',
  standalone: true,
  imports: [
    ExperimentDetailsComponent,
    ExperimentDescriptionComponent,
    StoichiometryTableComponent,
    ExperimentAttachmentsComponent,
  ],
  templateUrl: './experiment-info.component.html',
})
export class ExperimentInfoComponent {
  private experimentDetailService = inject(ExperimentDetailService);
  experimentId = computed(() => this.experimentDetailService.currentId());
}
