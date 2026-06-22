import { NotificationParams, NotificationType } from '@core/types/notification.i';
import { Component, inject } from '@angular/core';
import { MAT_SNACK_BAR_DATA } from '@angular/material/snack-bar';
import { MatIcon, MatIconRegistry } from '@angular/material/icon';
import { DomSanitizer } from '@angular/platform-browser';
import { NotificationService } from '@core/services/notification/notification.service';
import { ButtonComponent } from '../button/button.component';

@Component({
  selector: 'eln-notification',
  standalone: true,
  imports: [MatIcon, ButtonComponent],
  template: `
    <mat-icon [svgIcon]="params.type" class="icon type-icon" />

    <div class="flex min-w-0 flex-1 items-center gap-3">
      <span class="message flex-1 whitespace-pre-line">{{ params.message }}</span>

      @if (params.action) {
        <eln-button
          [type]="'button'"
          [variant]="'grey-outline'"
          [classList]="'!h-8 !rounded-md !border-neutral-300 !bg-transparent !px-3 !py-1 !text-neutral-900 hover:!bg-neutral-100 shrink-0'"
          (click)="onAction()"
        >
          {{ params.action.label }}
        </eln-button>
      }
    </div>

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

  onAction(): void {
    this.#notificationService.dismiss();
    this.params.action?.callback();
  }

  #registerIcon(name: string): void {
    this.#iconRegistry.addSvgIcon(name, this.#sanitizer.bypassSecurityTrustResourceUrl(`assets/${name}.svg`));
  }
}
