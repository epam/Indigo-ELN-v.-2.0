import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@core/services/api.service';
import { Notebook } from '@core/types/entities/notebook.i';
import { NotebookDialogData } from '@/core/types/entities/notebook-dialog-data.i';
import { CommonModule } from '@angular/common';
import { Component, Inject, inject, ViewChild } from '@angular/core';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { catchError } from 'rxjs';
import { NOTEBOOK_NAME_LENGTH } from '../notebook.constants';
import { FormErrorHandlerService } from '@core/services/error.service';

@Component({
  standalone: true,
  selector: 'eln-notebook-edit',
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    FormDialogComponent,
  ],
  templateUrl: './notebook-edit.component.html',
})
export class NotebookEditComponent {
  @ViewChild(FormDialogComponent) formDialog!: FormDialogComponent;
  notebookId: string;
  private errorHandler = inject(FormErrorHandlerService);
  dialogRef = inject(MatDialogRef);
  notebook: Partial<Notebook> = {};
  private serverErrorMessage = '';

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
          required: 'Notebook Name is required',
          'server-error': () => this.serverErrorMessage,
        },
      },
    },
    {
      type: 'editor',
      key: 'description',
      props: {
        label: 'Description',
        placeholder: 'Description',
      },
    },
  ];

  constructor(
    protected service: ApiService<Notebook>,
    @Inject(MAT_DIALOG_DATA) private data: NotebookDialogData,
  ) {
    if (data?.notebook) {
      this.notebook = {
        name: data.notebook.name,
        description: data.notebook.description,
      };
      this.notebookId = data.notebook.id;
    }
  }

  editNotebook(data: Notebook) {
    this.service
      .update(`notebooks/${this.notebookId}`, {
        ...data,
        description:
          typeof data.description === 'object'
            ? toHTML(data.description)
            : data.description,
      })
      .pipe(
        catchError((error) => {
          const result = this.errorHandler.handleError(error, {
            entityName: 'notebook',
            defaultErrorMessage:
              'There was an error updating the notebook, please try again later.',
            form: this.formDialog?.form,
            showToastOnDuplicate: true,
          });
          this.serverErrorMessage = result.serverErrorMessage;
          return result.observable;
        }),
      )
      .subscribe((result) => {
        if (result) this.dialogRef.close('refresh');
      });
  }
}
