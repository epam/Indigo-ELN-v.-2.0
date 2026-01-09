import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@core/services/api.service';
import { Notebook } from '@core/types/entities/notebook.i';
import { NotebookDialogData } from '@/core/types/entities/notebook-dialog-data.i';
import { CommonModule } from '@angular/common';
import { Component, Inject, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { catchError, of } from 'rxjs';
import { NOTEBOOK_NAME_LENGTH } from '../notebook.constants';

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
      props: {
        label: 'Notebook Name',
        placeholder: '00000000',
        required: true,
        minLength: NOTEBOOK_NAME_LENGTH,
        maxLength: NOTEBOOK_NAME_LENGTH,
        description: `Must be exactly ${NOTEBOOK_NAME_LENGTH} characters`,
      },
      validation: {
        messages: {
          minlength: `Must be exactly ${NOTEBOOK_NAME_LENGTH} characters`,
          maxlength: `Must be exactly ${NOTEBOOK_NAME_LENGTH} characters`,
          required: 'Name is required',
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
        catchError((editError) => {
          alert(
            `Error: ${editError?.error[0].message || 'There was an error updating notebook, please try again later.'}`,
          );
          return of(null);
        }),
      )
      .subscribe((result) => {
        if (result) this.dialogRef.close('refresh');
      });
  }
}
