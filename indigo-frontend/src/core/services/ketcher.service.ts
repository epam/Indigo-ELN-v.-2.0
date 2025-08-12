import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Observable } from 'rxjs';
import { KetcherPopupComponent } from '../components/ketcher/ketcher-popup/ketcher-popup.component';

@Injectable({
  providedIn: 'root',
})
export class KetcherService {
  constructor(private dialog: MatDialog) {}

  open(value?: string): Observable<string | undefined> {
    const dialogRef = this.dialog.open(KetcherPopupComponent, {
      width: '80vw',
      height: '80vh',
      data: { value },
    });

    return dialogRef.afterClosed();
  }
}
