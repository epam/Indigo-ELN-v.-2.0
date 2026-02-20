// import { Injectable, inject } from '@angular/core';
// import { AbstractControl, ValidationErrors } from '@angular/forms';
// import { Observable, of, timer } from 'rxjs';
// import { map, catchError, switchMap } from 'rxjs/operators';
// import { ApiService } from '@core/services/api.service';
// import { Notebook } from '@core/types/entities/notebook.i';

// @Injectable({ providedIn: 'root' })
// export class NotebookValidatorService {
//   private apiService = inject(ApiService<Notebook>);

//   /**
//    * Async validator to check if notebook name is unique
//    * @param excludeId - Optional notebook ID to exclude from uniqueness check (for edit mode)
//    */
//   uniqueNotebookName(
//     excludeId?: string,
//   ): (control: AbstractControl) => Observable<ValidationErrors | null> {
//     return (control: AbstractControl): Observable<ValidationErrors | null> => {
//       if (!control.value) {
//         return of(null);
//       }

//       // Debounce the validation to avoid excessive API calls
//       return timer(500).pipe(
//         switchMap(() =>
//           this.apiService.getList('notebooks', { name: control.value }).pipe(
//             map((notebooks) => {
//               // Filter out the current notebook if editing
//               const duplicates = excludeId
//                 ? notebooks.filter((notebook) => notebook.id !== excludeId)
//                 : notebooks;

//               return duplicates.length > 0 ? { notUnique: true } : null;
//             }),
//             catchError(() => of(null)), // Return null on error to avoid blocking form
//           ),
//         ),
//       );
//     };
//   }
// }
