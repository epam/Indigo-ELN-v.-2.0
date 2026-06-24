import { Injectable, inject } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ReportErrorDialogComponent } from '@core/components/common/report-error-dialog/report-error-dialog.component';
import { ReportErrorDialogData, ReportErrorFormValue } from '@core/types/report-error.i';

@Injectable({
  providedIn: 'root',
})
export class ReportErrorDialogService {
  private dialog = inject(MatDialog);

  open(data?: ReportErrorDialogData): MatDialogRef<ReportErrorDialogComponent, ReportErrorFormValue> {
    return this.dialog.open(ReportErrorDialogComponent, {
      data: data ?? null,
      autoFocus: false,
      restoreFocus: false,
    });
  }
}
