import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { Notebook } from '@/core/types/entities/notebook.i';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { catchError, of, tap } from 'rxjs';

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
  fields: FormlyFieldConfig[] = [
    {
      type: 'input',
      key: 'name',
      props: {
        label: 'Notebook Name',
        placeholder: 'Notebook Name',
        required: true,
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
        catchError((createError) => {
          alert(createError.message);
          return of(null);
        }),
      )
      .subscribe();
  }
}
