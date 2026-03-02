import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@core/services/api.service';
import { Notebook } from '@core/types/entities/notebook.i';
import { NotebookDialogData } from '@/core/types/entities/notebook-dialog-data.i';
import { CommonModule } from '@angular/common';
import { Component, Inject, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { NOTEBOOK_NAME_LENGTH } from '../notebook.constants';
import { of } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

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
  notebookId: string;
  dialogRef = inject(MatDialogRef);
  notebook: Partial<Notebook> = {};

  fields: FormlyFieldConfig[] = [
    {
      type: 'input',
      key: 'name',
      defaultValue: '',
      props: {
        label: 'Notebook Name',
        placeholder: '00000000',
        required: true,
        minLength: NOTEBOOK_NAME_LENGTH,
        maxLength: NOTEBOOK_NAME_LENGTH,
      },
      validators: {
        validation: [
          Validators.minLength(NOTEBOOK_NAME_LENGTH),
          Validators.maxLength(NOTEBOOK_NAME_LENGTH),
          Validators.pattern('^\\d+$'), // only digits
          Validators.required,
        ],
      },
      hooks: {
        onInit: (field) => {
          field.props['initialValue'] = field.formControl?.value;
        },
      },

      asyncValidators: {
        validation: [
          (control: any, field: any) => {
            const value: string = control.value;
            const initialValue = field.props['initialValue'];

            if (value === initialValue) {
              return of(null);
            }

            return of(value).pipe(
              switchMap((v: string) =>
                this.service.request<{ exists: boolean }>(
                  'get',
                  `notebooks/existence?name=${encodeURIComponent(v)}`,
                ),
              ),
              map((res) => (res?.exists ? { uniqueName: true } : null)),
              catchError(() => of(null)),
            );
          },
        ],
      },

      validation: {
        messages: {
          minlength: `Notebook Name is invalid, use ${NOTEBOOK_NAME_LENGTH} digits only`,
          maxlength: `Notebook Name is invalid, use ${NOTEBOOK_NAME_LENGTH} digits only`,
          pattern: `Notebook Name is invalid, use ${NOTEBOOK_NAME_LENGTH} digits only`,
          required: 'Notebook Name is required',
          uniqueName: 'Unique name is required',
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
      .subscribe(() => {
        this.dialogRef.close('refresh');
      });
  }
}
