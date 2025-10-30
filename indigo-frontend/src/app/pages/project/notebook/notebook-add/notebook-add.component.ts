import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { Notebook } from '@/core/types/entities/notebook.i';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
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
  dialogRef = inject(MatDialogRef);
  private snackBar = inject(MatSnackBar);
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

  constructor(protected service: ApiService<Notebook>) { }

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
        catchError((createError) => {
          this.snackBar.open(createError.error[0]?.message || createError.message || 'There was an error creating the notebook, please try again later.', 'Close', { duration: 5000 });
          return of(null);
        }),
      )
      .subscribe();
  }
}
