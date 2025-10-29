import { Component, Inject, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { ButtonComponent } from '../../common/button/button.component';
import { KetcherComponent } from '../../common/ketcher/ketcher.component';
import { Ketcher } from 'ketcher-core';
import { MutationBuilderService } from '@/core/services/experiment/mutation-builder.service';
import { Reaction } from '@/core/types/entities/experiments/experiment.i';

interface ModalData {
  height?: string; // Editor height
  width?: string; // Editor width
  isReaction: boolean; // Edit reaction if true, else edit molecule
  reaction?: Reaction; // Reaction data containing rxnfile
  molFile?: string; // Initial molecule file if editing molecule
}

@Component({
  selector: 'eln-structure-editor-modal',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    ButtonComponent,
    KetcherComponent,
  ],
  templateUrl: './structure-editor-modal.component.html',
})
export class StructureEditorModalComponent {
  @ViewChild('ketcherComponent') ketcherComponent!: KetcherComponent;

  private ketcherInstance: Ketcher | null = null;

  // Chemical editor configuration
  get editorHeight(): string {
    return this.data?.height || '500px';
  }

  get editorWidth(): string {
    return this.data?.width || '100%';
  }

  constructor(
    private dialogRef: MatDialogRef<StructureEditorModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ModalData,
    private mutationBuilder: MutationBuilderService,
  ) {}

  async onKetcherLoad(ketcher: Ketcher): Promise<void> {
    this.ketcherInstance = ketcher;

    // Load initial structure from reaction if available
    const initialStructure = this.data.isReaction
      ? this.data.reaction.rxnfile
      : this.data.molFile;
    if (initialStructure) {
      try {
        await this.ketcherInstance.setMolecule(initialStructure);
      } catch (error) {
        console.error('Error loading initial structure:', error);
      }
    }
  }

  async saveAndClose(): Promise<void> {
    if (!this.ketcherInstance) {
      console.warn('Ketcher not loaded yet');
      this.dialogRef.close({ success: false, error: 'Ketcher not loaded' });
      return;
    }

    try {
      if (this.data.isReaction) {
        // Get reaction anchor from experiment model
        const reactionAnchor = this.data?.reaction?.anchor;
        const mutations = await this.mutationBuilder.buildMutationsFromKetcher(
          this.ketcherInstance,
          reactionAnchor,
        );

        this.dialogRef.close({
          success: true,
          mutations,
        });
      } else {
        const molFile = await this.ketcherInstance.getMolfile();
        const image = await this.ketcherInstance.generateImage(molFile, {
          outputFormat: 'svg',
        });
        this.dialogRef.close({
          success: true,
          molFile: molFile,
          molFileImage: image,
        });
      }
    } catch (error) {
      console.error('Error processing Ketcher data:', error);
      this.dialogRef.close({
        success: false,
        error: 'Failed to process chemical structure data',
      });
    }
  }

  closeModal(): void {
    this.dialogRef.close();
  }
}
