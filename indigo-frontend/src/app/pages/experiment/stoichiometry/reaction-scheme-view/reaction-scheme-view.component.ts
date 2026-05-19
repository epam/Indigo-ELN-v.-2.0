import { Component, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { Mutation, ReactionAnchor } from '@/core/types/entities/experiments/mutation.i';
import {
  StructureEditorModalComponent,
  StructureEditorModalResult,
} from '@core/components/experiment/structure-editor-modal/structure-editor-modal.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { ApiImageComponent } from '@/core/components/common/image/api-image.component';
import { SvgIconComponent } from '@/core/components/common/svg-icon/svg-icon.component';
import { ApiService } from '@core/services/api.service';
import { AnalyzeRxnComponent } from '@pages/experiment/analyze-rxn/analyze-rxn.component';
import { SlideInPanelService } from '@core/components/common/slide-in-panel/slide-in-panel.service';
import { UUID } from '@core/types/entities/experiments/experiment-shared.i';

@Component({
  selector: 'eln-reaction-scheme-view',
  standalone: true,
  imports: [CommonModule, ButtonComponent, ApiImageComponent, SvgIconComponent],
  templateUrl: './reaction-scheme-view.component.html',
})
export class ReactionSchemeViewComponent {
  experimentId = input.required<UUID>();
  reactionAnchor = input.required<ReactionAnchor>();

  dialog = inject(MatDialog);
  experimentDetailService = inject(ExperimentDetailService);
  service = inject(ApiService);
  slideInPanelService = inject(SlideInPanelService);

  experiment = computed(() => this.experimentDetailService.experimentDetail());
  reaction = computed(() => this.experimentDetailService.getReaction(this.reactionAnchor()));

  reactionSchemeBlob = computed(() => {
    return this.experimentDetailService.updatedReactionImages().get(this.reaction().anchor);
  });

  reactionSchemeImageUrl = computed(() => {
    return `experiments/${this.experimentId()}/datamodel/reactions/${this.reaction().anchor}/picture?revision=${this.experimentDetailService.experimentDetail().revision}`;
  });

  openChemicalEditor(): void {
    const dialogRef = this.dialog.open(StructureEditorModalComponent, {
      width: '90vw',
      height: '80vh',
      maxWidth: '1200px',
      maxHeight: '800px',
      disableClose: false,
      data: {
        height: '600px',
        width: '100%',
        isReaction: true,
        reaction: this.reaction(),
      },
    });

    dialogRef.afterClosed().subscribe((result: StructureEditorModalResult) => {
      if (result?.success) {
        this.updateExperiment({
          type: 'SetScheme',
          anchor: this.reaction().anchor,
          rxnFile: result.molOrRxnFile,
        } as Mutation);
      }
    });
  }

  private updateExperiment(mutation: Mutation): void {
    this.experimentDetailService.updateDataModel(mutation).subscribe((response) => {
      if (response.unresolvedInputs && Object.keys(response.unresolvedInputs).length != 0) {
        this.slideInPanelService.open(AnalyzeRxnComponent, {
          inputs: {
            reaction: this.reaction(),
            unresolvedInputs: response.unresolvedInputs,
          },
        });
      }
    });
  }
}
