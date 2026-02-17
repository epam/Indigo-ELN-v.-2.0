import { HttpErrorResponse } from '@angular/common/http';
import { BackendError } from '@core/types/entities/base-entity.i';
import { NotificationType } from '@core/types/notification.i';
import { ErrorHandler, inject } from '@angular/core';
import { NotificationService } from '@core/services/notification/notification.service';

export class ELNErrorHandler implements ErrorHandler {
  notificationService = inject(NotificationService);

  handleError(error: unknown): void {
    try {
      const [message, log]: [string, string] = this.detectMessage(error);
      console.error(log, error);
      this.notificationService.notify({
        message,
        type: NotificationType.Error,
        isInline: false,
      });
    } catch (e) {
      console.error('Error handling error', e, error);
    }
  }

  private detectMessage(error: unknown): [string, string] {
    if (error instanceof HttpErrorResponse) {
      const commonLogMessage = `Server error calling ${error.url}: ${error.status} ${error.statusText}`;
      if (Array.isArray(error.error)) {
        const array: BackendError[] = error.error;
        const message = array
          .map((x) => (x.path ? `${x.path}: ${x.message}` : x.message))
          .join('\n');
        return [message, `${commonLogMessage}: ${message}`];
      }
      return ['Server error. Please try again later', commonLogMessage];
    }
    if (error instanceof Error && error.message != null) {
      return [error.message, error.message]
    }
    return ['Unknown error', 'Unknown error'];
  }
}
