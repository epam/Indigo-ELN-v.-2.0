import { Injectable, inject } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import {
  ReportErrorDialogComponent,
  ReportErrorDialogData,
  ReportErrorFormValue,
} from '@core/components/common/report-error-dialog/report-error-dialog.component';

export type ReportErrorDraft = ReportErrorDialogData;

@Injectable({
  providedIn: 'root',
})
export class ReportErrorDialogService {
  private dialog = inject(MatDialog);
  private lastErrorDraft: ReportErrorDraft | null = null;

  openBlank(): MatDialogRef<ReportErrorDialogComponent, ReportErrorFormValue> {
    return this.open();
  }

  openWithLastError(): MatDialogRef<ReportErrorDialogComponent, ReportErrorFormValue> {
    return this.open(this.lastErrorDraft ?? undefined);
  }

  openWithDraft(draft: ReportErrorDraft): MatDialogRef<ReportErrorDialogComponent, ReportErrorFormValue> {
    return this.open(draft);
  }

  setLastErrorDraft(draft: ReportErrorDraft): void {
    this.lastErrorDraft = draft;
  }

  clearLastErrorDraft(): void {
    this.lastErrorDraft = null;
  }

  open(initialValue?: ReportErrorDraft): MatDialogRef<ReportErrorDialogComponent, ReportErrorFormValue> {
    return this.dialog.open(ReportErrorDialogComponent, {
      data: initialValue ?? null,
      autoFocus: false,
      restoreFocus: false,
    });
  }
}
