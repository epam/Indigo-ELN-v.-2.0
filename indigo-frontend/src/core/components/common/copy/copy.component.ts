import { Component, inject, Input } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@core/types/notification.i';

@Component({
  selector: 'eln-copy',
  templateUrl: './copy.component.html',
  imports: [MatTooltipModule],
})
export class CopyComponent {
  @Input() text = '';

  private notificationService = inject(NotificationService);

  copy() {
    navigator.clipboard.writeText(this.text);
    this.notificationService.notify({
      message: 'Copied to clipboard',
      type: NotificationType.Success,
      isInline: false
    });
  }
}
