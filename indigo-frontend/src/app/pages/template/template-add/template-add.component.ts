import { Component, inject, OnInit } from '@angular/core';
import { FormDialogComponent } from '@core/components/common/form-dialog/form-dialog.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { ApiService } from '@core/services/api.service';
import { toHTML } from 'ngx-editor';
import { tap } from 'rxjs';
import { MatInputModule } from '@angular/material/input';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Template } from '@core/types/entities/template.i';

@Component({
  standalone: true,
  selector: 'eln-template-add',
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    FormDialogComponent,
  ],
  templateUrl: './template-add.component.html',
  styleUrl: './template-add.component.scss',
})
export class TemplateAddComponent implements OnInit {
  template!: Template;
  dialogRef = inject(MatDialogRef);
  data = inject(MAT_DIALOG_DATA);
  title = 'Add Template';
  submitAction: (data: Template) => void = this.createTemplates.bind(this);
  fields: FormlyFieldConfig[] = [
    {
      type: 'input',
      key: 'name',
      defaultValue: '',
      props: {
        label: 'Template Name',
        placeholder: 'Text',
        required: true,
      },
    },
  ];

  constructor(protected service: ApiService<any>) {}

  ngOnInit(): void {
    this.template = this.data?.template || null;

    if (this.template) {
      this.title = 'Edit Template';
      this.submitAction = this.updateTemplate.bind(this);
      this.fields = this.fields.map((field) => {
        field.defaultValue = this.template[`${field.key}`] || '';
        return field;
      });
    }
  }

  createTemplates(data: { name: string }) {
    this.service
      .create('templates', {
        ...data,
        templateTabs: [
          {
            name: '',
            components: [{}],
          },
        ],
      })
      .pipe(
        tap(() => {
          this.dialogRef.close('refresh');
        }),
      )
      .subscribe();
  }

  updateTemplate(data: Template) {
    this.service
      .update(`templates/${this.template.id}`, {
        ...data,
        description:
          typeof data.templateTabs === 'object'
            ? toHTML(data.templateTabs)
            : data.templateTabs,
      })
      .pipe(
        tap(() => {
          this.dialogRef.close('refresh');
        }),
      )
      .subscribe();
  }
}
