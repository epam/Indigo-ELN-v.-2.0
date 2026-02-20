// import { Injectable, inject } from '@angular/core';
// import {
//   HttpRequest,
//   HttpHandler,
//   HttpEvent,
//   HttpInterceptor,
//   HttpErrorResponse,
// } from '@angular/common/http';
// import { Observable, throwError } from 'rxjs';
// import { catchError } from 'rxjs/operators';
// import { NotificationService } from '@/core/services/notification/notification.service';
// import { NotificationType } from '@/core/types/notification.i';

// @Injectable()
// export class HttpErrorInterceptor implements HttpInterceptor {
//   private notificationService = inject(NotificationService);

//   intercept(
//     request: HttpRequest<unknown>,
//     next: HttpHandler,
//   ): Observable<HttpEvent<unknown>> {
//     return next.handle(request).pipe(
//       catchError((error: HttpErrorResponse) => {
//         // Extract error message from the response
//         const errorMessage =
//           error.error?.[0]?.message ||
//           error.error?.message ||
//           'An unexpected error occurred. Please try again later.';

//         // Show toast notification for all errors
//         this.notificationService.notify({
//           message: errorMessage,
//           type: NotificationType.Error,
//           isInline: false,
//         });

//         return throwError(() => error);
//       }),
//     );
//   }
// }
