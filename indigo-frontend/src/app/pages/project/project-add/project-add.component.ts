import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, ViewChild } from '@angular/core';
import { FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { catchError, tap } from 'rxjs';
import { Project } from '@core/types/entities/project.i';
import { Router } from '@angular/router';
import { FormErrorHandlerService } from '@core/services/error.service';
import { PROJECT_NAME_MAX_LENGTH } from '../project.constants';

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
  private errorHandler = inject(FormErrorHandlerService);
  @ViewChild(FormDialogComponent) formDialog!: FormDialogComponent;
  private serverErrorMessage = '';
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
      validators: {
        validation: [Validators.required],
      },
      validation: {
        messages: {
          required: 'Project Name is required',
          'server-error': () => this.serverErrorMessage,
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
        description:
          typeof data.description === 'object'
            ? toHTML(data.description)
            : data.description,
      })
      .pipe(
        tap((newProject: Project) => {
          this.dialogRef.close('refresh');
          this.router.navigate(['/projects', newProject.id]);
        }),
        catchError((error) => {
          const result = this.errorHandler.handleError(error, {
            entityName: 'project',
            defaultErrorMessage:
              'There was an error creating the project, please try again later.',
            form: this.formDialog?.form,
            showToastOnDuplicate: true,
            maxLengthMessage: `Project Name is too long, use ${PROJECT_NAME_MAX_LENGTH} characters maximum`,
          });
          this.serverErrorMessage = result.serverErrorMessage;
          return result.observable;
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
        }),
        catchError((error) => {
          const result = this.errorHandler.handleError(error, {
            entityName: 'project',
            defaultErrorMessage:
              'There was an error updating the project, please try again later.',
            form: this.formDialog?.form,
            showToastOnDuplicate: true,
            maxLengthMessage: `Project Name is too long, use ${PROJECT_NAME_MAX_LENGTH} characters maximum`,
          });
          this.serverErrorMessage = result.serverErrorMessage;
          return result.observable;
        }),
      )
      .subscribe();
  }
}
