import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { toHTML } from 'ngx-editor';
import { catchError, of, tap } from 'rxjs';
import { Project } from '@core/types/entities/project.i';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

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
      type: 'input',
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
        placeholder: 'Description',
      },
    },
  ];

  private snackBar = inject(MatSnackBar);
  private router = inject(Router);

  constructor(protected service: ApiService<any>) {
  }

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
          this.snackBar.open(
            `Project '${data.name}' has been successfully created`,
            'Close',
            {
              duration: 5000,
            },
          );
        }),
        catchError((createError) => {
          alert(createError.message);
          return of(null);
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

          this.snackBar.open(
            `Project '${data.name}' has been successfully updated`,
            'Close',
            {
              duration: 5000,
            },
          );
        }),
        catchError((updateError) => {
          alert(updateError.message);
          return of(null);
        }),
      )
      .subscribe();
  }
}
