import { NotificationComponent } from '@core/components/common/notification/notification.component';
import { inject, Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { NotificationParams } from '@core/types/notification.i';

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private snackBar = inject(MatSnackBar);

  notify(params: NotificationParams): void {
    this.snackBar.openFromComponent(NotificationComponent, {
      duration: 5000,
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: `notification-${params.type}-${params.isInline ? 'inline' : 'outline'}`,
      data: params,
    });
  }

  dismiss(): void {
    this.snackBar.dismiss();
  }
}
