import { Component, computed, inject, input } from '@angular/core';
import { CardComponent } from '@core/components/common/card/card.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { CdkAccordionModule } from '@angular/cdk/accordion';
import { ButtonComponent } from '@core/components/common/button/button.component';
import { ReactionViewComponent } from '@pages/experiment/stoichiometry/reaction-view/reaction-view.component';
import { SampleSearchComponent } from '@pages/experiment/sample-search/sample-search.component';
import { SlideInPanelService } from '@core/components/common/slide-in-panel/slide-in-panel.service';

@Component({
  selector: 'eln-stoichiometry-table',
  standalone: true,
  imports: [CardComponent, CdkAccordionModule, ReactionViewComponent, ButtonComponent],
  templateUrl: './stoichiometry-table.component.html',
})
export class StoichiometryTableComponent {
  experimentId = input.required<string>();

  private experimentDetailService = inject(ExperimentDetailService);
  private slideInPanel = inject(SlideInPanelService);

  private experiment = computed(() => this.experimentDetailService.experimentDetail());
  private model = computed(() => this.experimentDetailService.experimentModel());

  showAddMaterialDialog() {
    const [experiment, model] = [this.experiment(), this.model()];
    if (experiment && model) {
      const ref = this.slideInPanel.open(SampleSearchComponent, {
        inputs: { reactionAnchor: model.reactions[0]?.anchor },
      });
      ref.instance.close.subscribe(() => ref.close());
    }
  }
}
