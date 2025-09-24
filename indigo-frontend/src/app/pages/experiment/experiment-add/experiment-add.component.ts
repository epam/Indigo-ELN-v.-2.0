import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';

@Component({
  standalone: true,
  selector: 'app-experiment-add',
  imports: [
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule,
    FormDialogComponent,
  ],
  templateUrl: './experiment-add.component.html',
})
export class ExperimentAddComponent<T> implements OnInit {
  fields: FormlyFieldConfig[];

  ngOnInit(): void {
    this.fields = [
      {
        type: 'select',
        key: 'Select Template',
        className: 'flex-1',
        props: {
          label: 'Select Template',
          placeholder: 'Select Template',
          options: [{ value: 'project', label: 'project' }],
        },
      },
    ];
  }

  createExperiment(data: any) {}
}
