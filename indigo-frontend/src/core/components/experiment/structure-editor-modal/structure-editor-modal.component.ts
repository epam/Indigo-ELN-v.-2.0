import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { ButtonComponent } from "../../common/button/button.component";

interface ModalData {
  height?: string; // Editor height
  width?: string; // Editor width
}

@Component({
  selector: 'eln-structure-editor-modal',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, ButtonComponent],
  templateUrl: './structure-editor-modal.component.html',
})
export class StructureEditorModalComponent {
  // Chemical editor configuration
  get editorHeight(): string {
    return this.data?.height || '500px';
  }

  get editorWidth(): string {
    return this.data?.width || '100%';
  }

  constructor(
    private dialogRef: MatDialogRef<StructureEditorModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ModalData
  ) {}

  closeModal(): void {
    this.dialogRef.close();
  }

  saveAndClose(): void {
    // TODO: Implement save functionality when iframe communication is ready
    console.log('Save functionality will be implemented later');
    this.dialogRef.close();
  }
}