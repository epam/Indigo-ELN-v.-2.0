import { Component, input, output, inject, computed, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactionSchemeViewComponent } from '@core/components/experiment/reaction-scheme-view/reaction-scheme-view.component';
import { ReactionInputsTableComponent } from '../reaction-inputs-table/reaction-inputs-table.component';
import { ReactionProductsTableComponent } from '../reaction-products-table/reaction-products-table.component';
import { ExperimentModelService } from '@core/services/experiment/experiment-model.service';

@Component({
  selector: 'eln-reaction-view',
  standalone: true,
  imports: [
    CommonModule,
    ReactionSchemeViewComponent,
    ReactionInputsTableComponent,
    ReactionProductsTableComponent
  ],
  providers: [ExperimentModelService],
  templateUrl: './reaction-view.component.html',
})
export class ReactionViewComponent {
  private experimentModelService = inject(ExperimentModelService);

  experimentId = input<string | null>(null);
  modelUpdating = output<boolean>();

  // Computed signal for the first reaction (TODO: support multiple reactions)
  reaction = computed(() => this.experimentModelService.experimentModel()?.reactions?.[0] || null);

  constructor() {
    effect(() => {
      const id = this.experimentId();
      if (id) {
        this.experimentModelService.load(id);
      }
    });
  }

  onModelUpdating(isUpdating: boolean): void {
    this.modelUpdating.emit(isUpdating);
  }
}
