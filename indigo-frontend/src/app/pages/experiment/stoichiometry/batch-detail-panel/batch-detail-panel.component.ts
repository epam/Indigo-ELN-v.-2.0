import { Component, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BatchDetailStructureViewComponent } from '../batch-detail-structure-view/batch-detail-structure-view.component';
import { BatchDetailInfoPanelComponent } from '../batch-detail-info-panel/batch-detail-info-panel.component';
import { ReactionOutput, ReactionOutputSample } from '@core/types/entities/experiments/experiment.i';

export interface BatchDetailData {
  sample: ReactionOutputSample;
  output: ReactionOutput;
  experimentId: string;
}

@Component({
  selector: 'eln-batch-detail-panel',
  templateUrl: './batch-detail-panel.component.html',
  styleUrl: './batch-detail-panel.component.scss',
  imports: [
    CommonModule,
    BatchDetailStructureViewComponent,
    BatchDetailInfoPanelComponent,
  ],
  standalone: true,
})
export class BatchDetailPanelComponent {
  data = input.required<BatchDetailData>();
}
