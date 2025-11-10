import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@core/services/api.service';
import { Notebook } from '@core/types/entities/notebook.i';
import { NotebookDetail } from '@core/types/entities/notebook-detail.i';
import { CommonModule } from '@angular/common';
import { Component, Inject, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
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
  fields: FormlyFieldConfig[] = [
    {
      type: 'input',
      key: 'name',
      props: {
        label: 'Notebook Name',
        placeholder: 'Notebook Name',
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
      type: 'input',
      key: 'description',
      props: {
        label: 'Description',
        placeholder: 'Description',
      },
    },
  ];

  constructor(
    protected service: ApiService<Notebook>,
    @Inject(MAT_DIALOG_DATA) private data: { notebook: NotebookDetail }
  ) {
    if (data?.notebook?.id) {
      this.notebookId = data.notebook.id;
    }
  }

  editNotebook(data: Notebook) {
    this.service
      .update(`notebooks/${this.notebookId}`, {
        ...data,
      })
      .pipe(
        catchError((editError) => {
          alert(`Error: ${editError?.error[0].message || 'There was an error updating notebook, please try again later.'}`);
          return of(null);
        }),
      )
      .subscribe((result) => {
        if (result) this.dialogRef.close('refresh');
      });
  }
}
