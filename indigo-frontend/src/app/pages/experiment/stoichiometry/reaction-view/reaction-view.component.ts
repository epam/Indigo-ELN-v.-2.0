import { Component, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactionInputsTableComponent } from '../reaction-inputs-table/reaction-inputs-table.component';
import { ReactionProductsTableComponent } from '../reaction-products-table/reaction-products-table.component';
import { ReactionSchemeViewComponent } from '@pages/experiment/stoichiometry/reaction-scheme-view/reaction-scheme-view.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';
import { ReactionAnchor } from '@core/types/entities/experiments/mutation.i';

@Component({
  selector: 'eln-reaction-view',
  standalone: true,
  imports: [CommonModule, ReactionSchemeViewComponent, ReactionInputsTableComponent, ReactionProductsTableComponent],
  templateUrl: './reaction-view.component.html',
})
export class ReactionViewComponent {
  private experimentDetailService = inject(ExperimentDetailService);

  experimentId = input.required<UUID>();
  reactionAnchor = input.required<ReactionAnchor>();

  reactionScheme = input.required<boolean>();
  reactantsReagentsSolvents = input.required<boolean>();
  intendedProducts = input.required<boolean>();
}
