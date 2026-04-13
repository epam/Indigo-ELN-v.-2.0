import { Component, EventEmitter, inject, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { Mutation } from '@/core/types/entities/experiments/mutation.i';
import { Reaction } from '@/core/types/entities/experiments/experiment.i';
import { StructureEditorModalComponent } from '@core/components/experiment/structure-editor-modal/structure-editor-modal.component';
import { ExperimentDetailService } from '@core/services/experiment/experiment-detail.service';
import { ApiImageComponent } from '@/core/components/common/image/api-image.component';
import { SvgIconComponent } from '@/core/components/common/svg-icon/svg-icon.component';

@Component({
  selector: 'eln-reaction-scheme-view',
  standalone: true,
  imports: [CommonModule, ButtonComponent, ApiImageComponent, SvgIconComponent],
  templateUrl: './reaction-scheme-view.component.html',
})
export class ReactionSchemeViewComponent {
  @Input() reaction: Reaction | null = null;
  @Input() experimentId: string | null = null;
  @Output() modelUpdating = new EventEmitter<boolean>();

  dialog = inject(MatDialog);
  experimentDetailService = inject(ExperimentDetailService);

  reactionSchemeImageUrl(): string | null {
    if (!this.reaction) {
      return null;
    }
    return `experiments/${this.experimentId}/datamodel/reactions/${this.reaction.anchor}/picture?version=${this.reaction.rxnVersion}`;
  }

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

    dialogRef.afterClosed().subscribe((result) => {
      if (result?.success && result?.mutations) {
        this.updateExperimentWithMutations(result.mutations);
      } else if (result && !result.success) {
        console.error('Error in structure editor:', result.error);
      }
    });
  }

  private updateExperimentWithMutations(mutations: Mutation[]): void {
    if (!this.reaction) {
      console.error('No reaction available');
      return;
    }

    // Apply only the first mutation by the moment
    // TODO support multiple mutations
    if (mutations.length === 0) {
      console.warn('No mutations to apply');
      return;
    }

    const firstMutation = {
      ...mutations[0],
      anchor: this.reaction.anchor,
    };

    // Notify parent that update is starting
    this.modelUpdating.emit(true);

    this.experimentDetailService.updateDataModel(firstMutation).subscribe({
      next: () => {
        // Notify parent that update completed
        this.modelUpdating.emit(false);
      },
      error: (error) => {
        console.error('Failed to update experiment model:', error);
        // Notify parent that update completed (with error)
        this.modelUpdating.emit(false);
      },
    });
  }
}
