// src/core/services/crud-notification.service.ts
import { Injectable, inject } from '@angular/core';
import { NotificationService } from './notification.service';
import { NotificationType } from '@core/types/notification.i';

export type CrudEntity = 'Project' | 'Notebook'; // extend as needed
export type CrudAction = 'created' | 'updated' | 'deleted';

const ACTION_MESSAGES: Record<CrudAction, (entity: CrudEntity) => string> = {
  created: (entity) => `${entity} successfully created.`,
  updated: (entity) => `${entity} details successfully updated.`,
  deleted: (entity) => `${entity} successfully deleted.`,
};

@Injectable({ providedIn: 'root' })
export class SuccessNotificationService {
  private notificationService = inject(NotificationService);

  notifySuccess(entity: CrudEntity, action: CrudAction): void {
    this.notificationService.notify({
      message: ACTION_MESSAGES[action](entity),
      type: NotificationType.Success,
      isInline: false,
    });
  }
}
