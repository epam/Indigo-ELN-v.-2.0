import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { BackendError } from '@core/types/entities/base-entity.i';
import { NotificationType } from '@core/types/notification.i';
import { inject, Injectable } from '@angular/core';
import { NotificationService } from '@core/services/notification/notification.service';
import { catchError, Observable, throwError } from 'rxjs';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  notificationService = inject(NotificationService);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        try {
          const [message, log]: [string, string] = detectMessage(error);
          console.error(log, error);
          this.notificationService.notify({
            message,
            type: NotificationType.Error,
            isInline: false,
          });
        } catch (e) {
          console.error('Error handling error', e, error);
        }

        return throwError(() => error); // re-throw so individual components/services can still handle it if needed
      }),
    );
  }
}

function detectMessage(error: unknown): [string, string] {
  if (error instanceof HttpErrorResponse) {
    const commonLogMessage = `Server error calling ${error.url}: ${error.status} ${error.statusText}`;
    if (Array.isArray(error.error)) {
      const array: BackendError[] = error.error;
      const message = array.map((x) => (x.path ? `${x.path}: ${x.message}` : x.message)).join('\n');
      return [message, `${commonLogMessage}: ${message}`];
    }
    return ['Server error. Please try again later', commonLogMessage];
  }
  if (error instanceof Error && error.message != null) {
    return [error.message, error.message];
  }
  return ['Unknown error', 'Unknown error'];
}
