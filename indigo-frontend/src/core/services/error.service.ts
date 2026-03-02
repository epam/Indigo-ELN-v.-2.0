// // form-error-handler.service.ts
// import { Injectable, inject } from '@angular/core';
// import { FormGroup } from '@angular/forms';
// import { NotificationService } from '@/core/services/notification/notification.service';
// import { NotificationType } from '@/core/types/notification.i';
// import { of } from 'rxjs';

// export interface ErrorHandlerConfig {
//   entityName?: string;
//   defaultErrorMessage?: string;
//   showToastOnDuplicate?: boolean;
//   form?: FormGroup;
//   maxLengthMessage?: string;
// }

// @Injectable({ providedIn: 'root' })
// export class FormErrorHandlerService {
//   private notificationService = inject(NotificationService);

//   handleError(error: any, config: ErrorHandlerConfig = {}) {
//     const {
//       entityName = 'item',
//       defaultErrorMessage = `There was an error creating the ${entityName}, please try again later.`,
//       showToastOnDuplicate = true,
//       maxLengthMessage,
//       form,
//     } = config;

//     const errorMsg = error.error[0]?.message || defaultErrorMessage;

//     const fieldWithError = error.error[0]?.field || 'name';
//     const isAlreadyExists = errorMsg.toLowerCase().includes('already exists');

//     // Check if error is about field being too long
//     const isTooLong =
//       errorMsg.toLowerCase().includes('too long') ||
//       errorMsg.toLowerCase().includes('maximum length');

//     let serverErrorMessage: string;

//     if (isAlreadyExists) {
//       serverErrorMessage = `Unique ${fieldWithError} is required`;
//     } else if (isTooLong && maxLengthMessage) {
//       serverErrorMessage = maxLengthMessage;
//     } else {
//       serverErrorMessage = errorMsg;
//     }

//     // Set form errors if form is provided
//     if (form) {
//       const control = form.get(fieldWithError);
//       if (control) {
//         control.markAsTouched();
//         control.setErrors({ 'server-error': true });
//       }
//     }

//     // Show toast notification only if entity already exists and toast is enabled
//     if (isAlreadyExists && showToastOnDuplicate) {
//       this.notificationService.notify({
//         message: errorMsg,
//         type: NotificationType.Error,
//         isInline: false,
//       });
//     }

//     return { serverErrorMessage, observable: of(null) };
//   }
// }
