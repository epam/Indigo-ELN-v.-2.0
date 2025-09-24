import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';
import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { map, Observable } from 'rxjs';

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
  projectCodeItem$: Observable<DropdownMenuItem[]>;
  therapeuticItem$: Observable<DropdownMenuItem[]>;
  dialogRef = inject(MatDialogRef);
  dropdownMenuItem = [
    {
      label: 'Project',
      value: 'project',
    },
  ];

  fields: FormlyFieldConfig[];

  constructor(
    private service: ApiService<T>,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.getDictionaries();
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
      {
      fieldGroupClassName: 'flex flex-row gap-4',
      fieldGroup: [
      {
        type: 'select',
        key: 'Therapeutic Area',
        className: 'w-1/2',
        props: {
          label: 'Therapeutic Area',
          placeholder: 'Text',
          options: this.therapeuticItem$,
        },
      },
      {
        type: 'select',
        key: 'Cont. from Rxn',
        className: 'w-1/2',
        props: {
          label: 'Cont. from Rxn',
          placeholder: 'Text',
          options: this.dropdownMenuItem,
        },
      }
    ]},
    {
      fieldGroupClassName: 'flex flex-row gap-4',
      fieldGroup: [
      {
        type: 'select',
        key: 'Project Code & Name',
        className: 'w-1/2',
        props: {
          label: 'Project Code & Name',
          placeholder: 'Text',
          options: this.projectCodeItem$,
        },
      },
      {
        type: 'select',
        key: 'Cont. TO Rxn',
        className: 'w-1/2',
        props: {
          label: 'Cont. TO Rxn',
          placeholder: 'Text',
          options: this.dropdownMenuItem,
        },
      }
    ]},
    {
      fieldGroupClassName: 'flex flex-row gap-4',
      fieldGroup: [
      {
        type: 'chip-grid',
        key: 'Batch Creator',
        className: 'w-1/2',
        props: {
          label: 'Batch Creator',
          placeholder: 'Add Text',
        },
      },
      {
        type: 'input',
        key: 'Project Alias Name',
        className: 'w-1/2',
        props: {
          label: 'Project Alias Name',
          placeholder: 'Text',
          options: this.dropdownMenuItem,
        },
      }
    ]},
    {
      type: 'input',
      key: 'literature',
      className: 'flex-1',
      props: {
        label: 'Literature',
        placeholder: 'Literature',
      },
    }
  ];
  }

  getDictionaries() {
    this.therapeuticItem$ = this.service
      .getDictionary<
        { id: string; name: string }[]
      >('dictionaries/THERAPEUTIC_AREA')
      .pipe(map((items) => items.map((e) => ({ label: e.name, value: e.id }))));
    this.projectCodeItem$ = this.service
      .getDictionary<
        { id: string; name: string }[]
      >('dictionaries/PROJECT_CODE')
      .pipe(map((items) => items.map((e) => ({ label: e.name, value: e.id }))));
  }

  createExperiment(data: any) {
  }
}
