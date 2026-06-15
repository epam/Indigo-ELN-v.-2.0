import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { ButtonComponent } from '../button/button.component';

export interface RemoveMemberConfirmationDialogData {
  showCascadeCheckbox: boolean;
  cascadeCheckboxLabel: string;
}

export interface RemoveMemberConfirmationResult {
  confirmed: boolean;
  removeFromChildren: boolean;
}

@Component({
  selector: 'eln-remove-member-confirmation-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule, MatDialogModule, ButtonComponent],
  templateUrl: './remove-member-confirmation-dialog.component.html',
})
export class RemoveMemberConfirmationDialogComponent {
  readonly data = inject<RemoveMemberConfirmationDialogData>(MAT_DIALOG_DATA);
  private dialogRef = inject(MatDialogRef<RemoveMemberConfirmationDialogComponent, RemoveMemberConfirmationResult>);

  readonly cascadeCheckboxLabel = this.data.cascadeCheckboxLabel;

  // Project/Notebook show cascade option (default true), Experiment does not (default false).
  removeFromChildren = this.data.showCascadeCheckbox;

  cancel(): void {
    this.dialogRef.close({
      confirmed: false,
      removeFromChildren: false,
    });
  }

  confirm(): void {
    this.dialogRef.close({
      confirmed: true,
      removeFromChildren: this.removeFromChildren,
    });
  }
}
