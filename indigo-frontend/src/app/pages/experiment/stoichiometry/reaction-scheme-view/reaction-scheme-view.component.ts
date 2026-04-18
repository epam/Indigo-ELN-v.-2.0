import { Component, computed, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { Mutation } from '@/core/types/entities/experiments/mutation.i';
import { Reaction } from '@/core/types/entities/experiments/experiment.i';
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

@Component({
  selector: 'eln-reaction-scheme-view',
  standalone: true,
  imports: [CommonModule, ButtonComponent, ApiImageComponent, SvgIconComponent],
  templateUrl: './reaction-scheme-view.component.html',
})
export class ReactionSchemeViewComponent {
  @Input({ required: true }) reaction: Reaction | null = null;
  @Input({ required: true }) experimentId: string;

  dialog = inject(MatDialog);
  experimentDetailService = inject(ExperimentDetailService);
  service = inject(ApiService);
  slideInPanelService = inject(SlideInPanelService);

  reactionSchemeBlob = computed(() => {
    if (!this.reaction) {
      return null;
    }
    return this.experimentDetailService.updatedReactionImages().get(this.reaction.anchor);
  });

  reactionSchemeImageUrl = computed(() => {
    if (!this.reaction) {
      return null;
    }
    return `experiments/${this.experimentId}/datamodel/reactions/${this.reaction.anchor}/picture?revision=${this.experimentDetailService.experimentDetail().revision}`;
  });

  openChemicalEditor(): void {
    if (!this.experimentId) {
      console.warn('No experiment ID available');
      return;
    }

    this.openModal();
  }

  private openModal(): void {
    if (!this.reaction) {
      console.warn('No reaction available');
      return;
    }

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
        reaction: this.reaction,
      },
    });

    dialogRef.afterClosed().subscribe((result: StructureEditorModalResult) => {
      if (result?.success) {
        this.updateExperiment({
          type: 'SetScheme',
          anchor: this.reaction.anchor,
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
            reaction: this.experimentDetailService.getReaction(this.reaction.anchor),
            unresolvedInputs: response.unresolvedInputs,
          },
        });
      }
    });
  }
}
