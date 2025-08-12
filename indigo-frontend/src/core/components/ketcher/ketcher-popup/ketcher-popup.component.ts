import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { KetcherComponent } from '../ketcher/ketcher.component';
import { MatDialogModule } from '@angular/material/dialog';
import { ButtonComponent } from '@/core/components/common/button/button.component';

@Component({
  selector: 'eln-ketcher-popup',
  standalone: true,
  imports: [KetcherComponent, MatDialogModule, ButtonComponent],
  templateUrl: './ketcher-popup.component.html',
  styleUrls: ['./ketcher-popup.component.scss'],
})
export class KetcherPopupComponent {
  value?: string;

  constructor(
    public dialogRef: MatDialogRef<KetcherPopupComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { value?: string }
  ) {
    this.value = data.value;
  }

  onSave() {
    this.dialogRef.close(this.value);
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
