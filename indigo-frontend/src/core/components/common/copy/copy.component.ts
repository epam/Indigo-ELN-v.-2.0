import { Component, inject, Input } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'eln-copy',
  templateUrl: './copy.component.html',
  imports: [MatTooltipModule],
})
export class CopyComponent {
  @Input() text = '';

  private snackBar = inject(MatSnackBar);

  copy() {
    navigator.clipboard.writeText(this.text);
    this.snackBar.open('Copied to clipboard', null,
      {
        duration: 5000,
      }
    );
  }
}
