import { Component, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactionInputsTableComponent } from '../reaction-inputs-table/reaction-inputs-table.component';
import { ReactionProductsTableComponent } from '../reaction-products-table/reaction-products-table.component';
import { ProductBatchSummaryTableComponent } from '../product-batch-summary-table/product-batch-summary-table.component';
import { ReactionSchemeViewComponent } from '@pages/experiment/stoichiometry/reaction-scheme-view/reaction-scheme-view.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';

@Component({
  selector: 'eln-reaction-view',
  standalone: true,
  imports: [
    CommonModule,
    ReactionSchemeViewComponent,
    ReactionInputsTableComponent,
    ReactionProductsTableComponent,
    ProductBatchSummaryTableComponent,
  ],
  templateUrl: './reaction-view.component.html',
})
export class ReactionViewComponent {
  private experimentDetailService = inject(ExperimentDetailService);

  experimentId = input<string | null>(null);

  // Computed signal for the first reaction (TODO: support multiple reactions)
  reaction = computed(() => this.experimentDetailService.experimentModel()?.reactions?.[0] || null);
}
