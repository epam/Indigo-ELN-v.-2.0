import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { of, tap } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';
import { Project } from '@core/types/entities/project.i';
import { Router } from '@angular/router';
import { PROJECT_NAME_MAX_LENGTH } from '../project.constants';
import { NotificationType } from '@/core/types/notification.i';
import { NotificationService } from '@/core/services/notification/notification.service';

@Component({
  standalone: true,
  selector: 'eln-project-add',
  imports: [MatInputModule, FormsModule, ReactiveFormsModule, CommonModule, FormDialogComponent],
  templateUrl: './project-add.component.html',
})
export class ProjectAddComponent implements OnInit {
  project!: Project;
  dialogRef = inject(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  title = 'Add Project';
  submitAction: (data: Project) => void = this.createProject.bind(this);
  notificationService = inject(NotificationService);
  uniqueNameToastMessage = signal('');

  fields: FormlyFieldConfig[] = [
    {
      type: 'input',
      key: 'name',
      defaultValue: '',
      props: {
        label: 'Project Name',
        placeholder: 'Project Name',
        required: true,
      },
      validators: {
        validation: [Validators.required, Validators.maxLength(PROJECT_NAME_MAX_LENGTH)],
      },
      asyncValidators: {
        validation: [
          (control: any) => {
            const value: string = control.value;
            if (this.project && value === this.project.name) {
              return of(null);
            }
            return of(value).pipe(
              switchMap((v: string) => {
                this.uniqueNameToastMessage.set(`Project with name '${v}' already exists`);
                return this.service.request<{ exists: boolean }>(
                  'get',
                  `projects/existence?name=${encodeURIComponent(v)}`,
                );
              }),
              map((res) => (res?.exists ? { uniqueName: true } : null)),
              catchError(() => of(null)),
            );
          },
        ],
      },

      validation: {
        messages: {
          required: 'Project Name is required',
          maxlength: `Project Name is too long, use ${PROJECT_NAME_MAX_LENGTH} characters maximum`,
          uniqueName: 'Unique name is required',
        },
      },
    },
    {
      type: 'chip-grid',
      key: 'keywords',
      defaultValue: [],
      props: {
        label: 'Project Keywords',
        placeholder: 'Add Keyword',
        suggest: (query: string) =>
          this.service.request<string[]>('get', `projects/keywords/suggest?search=${encodeURIComponent(query)}`),
      },
    },
    {
      type: 'editor',
      key: 'literature',
      defaultValue: '',
      props: {
        label: 'Literature',
        placeholder: 'Literature',
      },
    },
    {
      type: 'editor',
      key: 'description',
      defaultValue: '',
      props: {
        label: 'Project Description',
        placeholder: 'Project Description',
      },
    },
  ];

  private router = inject(Router);

  constructor(protected service: ApiService<any>) {}

  ngOnInit(): void {
    this.project = this.data?.project || null;

    if (this.project) {
      this.title = 'Edit Project';
      this.submitAction = this.updateProject.bind(this);
      this.fields = this.fields.map((field) => {
        field.defaultValue = this.project[`${field.key}`] || '';
        return field;
      });
    }
  }

  createProject(data: Project): void {
    this.service
      .create('projects', {
        ...data,
        description: typeof data.description === 'object' ? toHTML(data.description) : data.description,
      })
      .subscribe((newProject: Project) => {
        this.notificationService.notify({
          message: 'Project successfully created.',
          type: NotificationType.Success,
          isInline: false,
        });
        this.dialogRef.close('refresh');
        this.router.navigate(['/projects', newProject.id]);
      });
  }

  updateProject(data: Project) {
    this.service
      .update(`projects/${this.project.id}`, {
        ...data,
        description: typeof data.description === 'object' ? toHTML(data.description) : data.description,
      })
      .pipe(
        tap(() => {
          this.notificationService.notify({
            message: 'Project details successfully updated.',
            type: NotificationType.Success,
            isInline: false,
          });
          this.dialogRef.close('refresh');
        }),
      )
      .subscribe();
  }
}
