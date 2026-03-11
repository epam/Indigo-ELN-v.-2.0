import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@core/services/api.service';
import { Notebook } from '@core/types/entities/notebook.i';
import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { NOTEBOOK_NAME_LENGTH } from '../notebook.constants';
import { of, switchMap, map, catchError } from 'rxjs';
import { SuccessNotificationService } from '@/core/services/notification/success.notification.component';

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
  successNotificationService = inject(SuccessNotificationService);

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
          Validators.pattern('^\\d+$'), // only digits
        ],
      },
      asyncValidators: {
        validation: [
          (control: any) => {
            const value: string = control.value;
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
        label: 'Notebook Description',
        placeholder: 'Notebook Description',
      },
    },
  ];

  constructor(protected service: ApiService<Notebook>) {}
  get uniqueNameToastMessage(): string {
    const name = this.fields[0]?.formControl?.value ?? '';
    return `Notebook with name '${name}' already exists`;
  }
  createNotebook(data: Notebook) {
    this.service
      .create(`projects/${this.projectId}/notebooks`, {
        ...data,
        description:
          typeof data.description === 'object'
            ? toHTML(data.description)
            : data.description,
      })
      .subscribe(() => {
        this.successNotificationService.notifySuccess('Notebook', 'created');
        this.dialogRef.close('refresh');
      });
  }
}
