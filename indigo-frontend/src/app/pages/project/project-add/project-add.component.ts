import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { catchError, EMPTY, tap } from 'rxjs';
import { Project } from '@core/types/entities/project.i';
import { Router } from '@angular/router';
import { NotificationService } from '@core/services/notification/notification.service';
import { NotificationType } from '@/core/types/notification.i';

@Component({
  standalone: true,
  selector: 'eln-project-add',
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    FormDialogComponent,
  ],
  templateUrl: './project-add.component.html',
})
export class ProjectAddComponent implements OnInit {
  project!: Project;
  dialogRef = inject(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  title = 'Add Project';
  submitAction: (data: Project) => void = this.createProject.bind(this);
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
    },
    {
      type: 'chip-grid',
      key: 'keywords',
      defaultValue: [],
      props: {
        label: 'Project Keywords',
        placeholder: 'Add Keyword',
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
  private notificationService = inject(NotificationService);

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
        description:
          typeof data.description === 'object'
            ? toHTML(data.description)
            : data.description,
      })
      .pipe(
        tap((newProject: Project) => {
          this.dialogRef.close('refresh');
          this.router.navigate(['/projects', newProject.id]);

          this.notificationService.notify({
            message: `Project '${data.name}' has been successfully created`,
            type: NotificationType.Success,
            isInline: false,
          });
        }),
        catchError((createError) => {
          this.notificationService.notify({
            message: createError.message,
            type: NotificationType.Error,
            isInline: false,
          });
          return EMPTY;
        }),
      )
      .subscribe();
  }

  updateProject(data: Project) {
    this.service
      .update(`projects/${this.project.id}`, {
        ...data,
        description:
          typeof data.description === 'object'
            ? toHTML(data.description)
            : data.description,
      })
      .pipe(
        tap(() => {
          this.dialogRef.close('refresh');

          this.notificationService.notify({
            message: `Project details successfully updated.`,
            type: NotificationType.Success,
            isInline: false,
          });
        }),
        catchError((updateError) => {
          this.notificationService.notify({
            message: updateError.message,
            type: NotificationType.Error,
            isInline: false,
          });

          return EMPTY;
        }),
      )
      .subscribe();
  }
}
