import {
  NotificationParams,
  NotificationType,
} from '@core/types/notification.i';
import { Component, inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';
import { MatIcon, MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { NotificationService } from '@core/services/notification/notification.service';

@Component({
  selector: 'eln-notification',
  standalone: true,
  imports: [MatIcon],
  template: `
    <mat-icon [svgIcon]="params.type" class="icon type-icon" />

    <span class="message" style="flex: 1;">{{
      params.message
    }}</span>

    <button class="close-button" (click)="onClose()">
      <mat-icon svgIcon="close" class="icon" />
    </button>
  `,
})
export class NotificationComponent {
  readonly params = inject<NotificationParams>(MAT_SNACK_BAR_DATA);

  readonly #iconRegistry = inject(MatIconRegistry);
  readonly #sanitizer = inject(DomSanitizer);
  readonly #notificationService = inject(NotificationService);

  constructor() {
    Object.values(NotificationType).forEach((type) => {
      this.#registerIcon(type);
    });

    this.#registerIcon('close');
  }

  onClose(): void {
    this.#notificationService.dismiss();
  }

  #registerIcon(name: string): void {
    this.#iconRegistry.addSvgIcon(
      name,
      this.#sanitizer.bypassSecurityTrustResourceUrl(`assets/${name}.svg`),
    );
  }
}
