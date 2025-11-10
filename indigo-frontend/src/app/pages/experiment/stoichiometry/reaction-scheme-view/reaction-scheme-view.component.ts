import { Component, inject, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { ButtonComponent } from '@/core/components/common/button/button.component';
import { ExperimentModelService } from '@/core/services/experiment/experiment-model.service';
import { MutateModelForm } from '@/core/types/entities/experiments/experiment-mutate-form.i';
import { Mutation } from '@/core/types/entities/experiments/mutation.i';
import { Reaction } from '@/core/types/entities/experiments/experiment.i';
import {
  StructureEditorModalComponent
} from '@core/components/experiment/structure-editor-modal/structure-editor-modal.component';

@Component({
  selector: 'eln-reaction-scheme-view',
  standalone: true,
  imports: [CommonModule, ButtonComponent],
  templateUrl: './reaction-scheme-view.component.html',
})
export class ReactionSchemeViewComponent {
  @Input() reaction: Reaction | null = null;
  @Input() experimentId: string | null = null;
  @Output() modelUpdating = new EventEmitter<boolean>();

  dialog = inject(MatDialog);
  experimentModelService = inject(ExperimentModelService);

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
      anchor: this.reaction.anchor
    };

    const payload: MutateModelForm = {
      model: this.experimentModelService.experimentModel(),
      mutation: firstMutation,
    };

    // Notify parent that update is starting
    this.modelUpdating.emit(true);

    this.experimentModelService
      .updateDataModel(this.experimentId!, payload)
      .subscribe({
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
