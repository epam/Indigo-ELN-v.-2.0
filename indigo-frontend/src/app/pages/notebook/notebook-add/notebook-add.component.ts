import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@core/services/api.service';
import { Notebook } from '@core/types/entities/notebook.i';
import { CommonModule } from '@angular/common';
import { Component, inject, ViewChild } from '@angular/core';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
// import { MatSnackBar } from '@angular/material/snack-bar';
import { NotificationService } from '@/core/services/notification/notification.service';
import { NotificationType } from '@/core/types/notification.i';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { catchError, of, tap } from 'rxjs';
import { NOTEBOOK_NAME_LENGTH } from '../notebook.constants';

@Component({
  standalone: true,
  selector: 'eln-notebook-add',
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    FormDialogComponent,
  ],
  templateUrl: './notebook-add.component.html',
})
export class NotebookAddComponent {
  projectId: string;
  private notificationService = inject(NotificationService);

  dialogRef = inject(MatDialogRef);
  @ViewChild(FormDialogComponent) formDialog!: FormDialogComponent;
  private serverErrorMessage = '';
  // private snackBar = inject(MatSnackBar);
  fields: FormlyFieldConfig[] = [
    {
      type: 'input',
      key: 'name',
      props: {
        label: 'Notebook Name',
        placeholder: '00000000',
        required: true,
        minLength: NOTEBOOK_NAME_LENGTH,
        maxLength: NOTEBOOK_NAME_LENGTH,
      },
      validators: {
        validation: [
          Validators.required,
          Validators.minLength(NOTEBOOK_NAME_LENGTH),
          Validators.maxLength(NOTEBOOK_NAME_LENGTH),
        ],
      },
      validation: {
        messages: {
          minlength: `Notebook Name is invalid, use ${NOTEBOOK_NAME_LENGTH} digits only`,
          maxlength: `Notebook Name is invalid, use ${NOTEBOOK_NAME_LENGTH} digits only`,
          required: 'Name is required',
          'server-error': () => this.serverErrorMessage,
        },
      },
    },
    {
      type: 'editor',
      key: 'description',
      props: {
        label: 'Notebook Description',
        placeholder: 'Notebook Description',
      },
    },
  ];

  constructor(protected service: ApiService<Notebook>) {}

  createNotebook(data: Notebook) {
    this.service
      .create(`projects/${this.projectId}/notebooks`, {
        ...data,
        description:
          typeof data.description === 'object'
            ? toHTML(data.description)
            : data.description,
      })
      .pipe(
        tap(() => {
          this.dialogRef.close('refresh');
        }),
        catchError((editError) => {
          const errorMsg =
            editError.error[0]?.message ||
            'There was an error creating the notebook, please try again later.';

          const fieldWithError = editError.error[0]?.field || 'name';
          const isNameExists = errorMsg
            .toLowerCase()
            .includes('already exists');
          this.serverErrorMessage = isNameExists
            ? 'Unique name is required'
            : errorMsg;

          if (this.formDialog?.form) {
            const control = this.formDialog.form.get(fieldWithError);
            if (control) {
              control.markAsTouched();
              control.setErrors({ 'server-error': true });
            }
          }
          if (isNameExists) {
            this.notificationService.notify({
              message: errorMsg,
              type: NotificationType.Error,
              isInline: false,
            });
          }
          return of(null);
        }),
      )
      .subscribe();
  }
}
