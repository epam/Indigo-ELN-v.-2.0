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
import { Reaction } from '@/core/types/entities/experiments/experiment.i';
import { Ketcher } from 'ketcher-core';

interface ModalData {
  height?: string; // Editor height
  width?: string; // Editor width
  isReaction?: boolean; // Edit reaction if true, else edit molecule, allow both if null
  reaction?: Reaction; // Reaction data containing rxnfile
  molFile?: string; // Initial molecule file if editing molecule
}

export interface StructureEditorModalSuccess {
  success: true;
  isReaction: boolean;
  molOrRxnFile: string;
  image: Blob;
}

export interface StructureEditorModalFailure {
  success: false;
  error: string;
}

export type StructureEditorModalResult =
  | StructureEditorModalSuccess
  | StructureEditorModalFailure;

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

  initialized = false;
  initialStructure: string;

  // Chemical editor configuration
  get editorHeight(): string {
    return this.data?.height || '500px';
  }

  get editorWidth(): string {
    return this.data?.width || '100%';
  }

  constructor(
    private dialogRef: MatDialogRef<
      StructureEditorModalComponent,
      StructureEditorModalResult
    >,
    @Inject(MAT_DIALOG_DATA) public data: ModalData,
  ) {
    this.initialStructure =
      this.data.isReaction === true
        ? this.data.reaction.rxnfile
        : this.data.molFile;
  }

  async onKetcherLoad(ketcher: Ketcher): Promise<void> {
    // Load initial structure from reaction if available
    const initialStructure =
      this.data.isReaction === true
        ? this.data.reaction.rxnfile
        : this.data.molFile;
    if (initialStructure) {
      try {
        await ketcher.setMolecule(initialStructure);
      } catch (error) {
        console.error('Error loading initial structure:', error);
      }
    }
    this.initialized = true;
  }

  async saveAndClose(): Promise<void> {
    if (!this.initialized) {
      return;
    }
    try {
      const molOrRxnFile = await this.ketcherComponent.getRxnOrMolfile(
        this.data.isReaction,
      );
      const image = await this.ketcherComponent.generateImage(molOrRxnFile);
      const isReaction =
        this.data.isReaction != null
          ? this.data.isReaction
          : this.ketcherComponent.containsReaction();
      this.dialogRef.close({
        success: true,
        isReaction,
        molOrRxnFile,
        image,
      });
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
