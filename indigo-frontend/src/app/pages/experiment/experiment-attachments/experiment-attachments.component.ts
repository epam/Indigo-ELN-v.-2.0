import { Component, computed, inject, input } from '@angular/core';
import { CardComponent } from '@core/components/common/card/card.component';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { AttachmentsComponent } from '@core/components/common/attachments/attachments.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { Attachment } from '@core/types/entities/attachment.i';

@Component({
  selector: 'eln-experiment-attachments',
  standalone: true,
  imports: [CardComponent, CdkAccordionModule, AttachmentsComponent],
  templateUrl: './experiment-attachments.component.html',
})
export class ExperimentAttachmentsComponent {
  experimentId = input.required<string>();

  experimentDetailService = inject(ExperimentDetailService);

  experiment = computed(() => this.experimentDetailService.experimentDetail());

  onAttachmentsChanged(attachments: Attachment[]) {
    this.experimentDetailService.dataModelUpdated((model) => ({ ...model, attachments: [...attachments] }));
  }
}
