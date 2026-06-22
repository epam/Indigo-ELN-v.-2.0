import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { BackendError } from '@core/types/entities/base-entity.i';
import { NotificationType } from '@core/types/notification.i';
import { inject, Injectable } from '@angular/core';
import { NotificationService } from '@core/services/notification/notification.service';
import { ReportErrorDialogService } from '@core/services/report-error-dialog.service';
import { ReportErrorContext } from '@core/components/common/report-error-dialog/report-error-dialog.component';
import { catchError, Observable, throwError } from 'rxjs';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  notificationService = inject(NotificationService);
  reportErrorDialogService = inject(ReportErrorDialogService);

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        try {
          const [message, log]: [string, string] = detectMessage(error);
          const reportDraft = buildReportErrorDraft(req, error, message);
          if (!isReportBugRequest(req)) {
            this.reportErrorDialogService.setLastErrorDraft(reportDraft);
          }
          console.error(log, error);
          this.notificationService.notify({
            message,
            type: NotificationType.Error,
            isInline: false,
            ...(isReportBugRequest(req)
              ? {}
              : {
                  action: {
                    label: 'Report Error',
                    callback: () => this.reportErrorDialogService.openWithLastError(),
                  },
                }),
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

function buildReportErrorDraft(
  req: HttpRequest<unknown>,
  error: HttpErrorResponse,
  message: string,
): { initialValue: { title: string; problemDescription: string }; context: ReportErrorContext } {
  const requestBody = normalizeErrorBody(req.body);
  const responseBody = normalizeErrorBody(error.error);

  return {
    initialValue: {
      title: `Backend error ${error.status || 'unknown'}`,
      problemDescription: message,
    },
    context: {
      requestURL: req.urlWithParams,
      requestMethod: req.method,
      ...(requestBody ? { requestBody } : {}),
      ...(responseBody ? { responseBody } : {}),
    },
  };
}

function normalizeErrorBody(errorBody: unknown): string | null {
  if (errorBody == null) {
    return null;
  }

  if (typeof errorBody === 'string') {
    return errorBody;
  }

  if (Array.isArray(errorBody)) {
    return errorBody
      .map((item) => {
        if (item && typeof item === 'object' && 'message' in item) {
          const path = 'path' in item && typeof item.path === 'string' ? `${item.path}: ` : '';
          const itemMessage = typeof item.message === 'string' ? item.message : JSON.stringify(item);
          return `${path}${itemMessage}`;
        }

        return typeof item === 'string' ? item : JSON.stringify(item);
      })
      .join('\n');
  }

  if (typeof errorBody === 'object') {
    try {
      return JSON.stringify(errorBody, null, 2);
    } catch {
      return 'Unable to serialize error details';
    }
  }

  return String(errorBody);
}

function isReportBugRequest(req: HttpRequest<unknown>): boolean {
  return req.url.includes('/api/eln/report-bug') || req.url.endsWith('report-bug');
}
