import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { catchError, of } from 'rxjs';

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
export class ProjectAddComponent {
  fields: FormlyFieldConfig[] = [
    {
      type: 'input',
      key: 'name',
      props: {
        label: 'Project Name',
        placeholder: 'Project Name',
        required: true,
      },
    },
    {
      type: 'chip-grid',
      key: 'keywords',
      props: {
        label: 'Project Keywords',
        placeholder: 'Add Keyword',
      },
    },
    {
      type: 'input',
      key: 'literature',
      props: {
        label: 'Literature',
        placeholder: 'Literature',
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

  constructor(protected service: ApiService<any>) {}

  createProject(data: any) {
    this.service
      .create(data)
      .pipe(
        catchError((createError) => {
          alert(createError.message);
          return of(null);
        }),
      )
      .subscribe();
  }
}
