import { ButtonComponent } from '@/core/components/common/button/button.component';
import { ChipComponent } from '@/core/components/common/chip/chip.component';
import { DropdownMenuItem } from '@/core/components/common/dropdown-menu/dropdown-menu.i';
import { InputComponent } from '@/core/components/common/input/input.component';
import { ModalComponent } from '@/core/components/common/modal/modal.component';
import { SelectComponent } from '@/core/components/common/select/select.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { Component, OnInit, viewChild } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { map, Observable } from 'rxjs';

@Component({
  standalone: true,
  selector: 'app-experiment-add',
  imports: [
    ModalComponent,
    ButtonComponent,
    InputComponent,
    SelectComponent,
    MatInputModule,
    FormsModule,
    ReactiveFormsModule,
    ChipComponent,
    CommonModule,
  ],
  templateUrl: './experiment-add.component.html',
})
export class ExperimentAddComponent<T> implements OnInit {
  readonly modalComponent =
    viewChild.required<ModalComponent>('addExperimentModal');
  formGroup!: FormGroup;
  chips: string[] = [];
  linkedExperiments: string[] = [];
  batchCreators: string[] = [];
  isSubmitted = false;
  dropdownMenuItem = [
    {
      label: 'Project',
      value: 'project',
    },
  ];
  projectCodeItem$: Observable<DropdownMenuItem[]>;
  theraputicItem$: Observable<DropdownMenuItem[]>;

  constructor(
    private service: ApiService<T>,
    private fb: FormBuilder,
  ) {}

  ngOnInit(): void {
    this.service.setup('projects');
    this.getDictionaries();
    this.formGroup = this.fb.group({
      name: ['', [Validators.required]],
      template: [['', true]],
      therapeuticArea: [''],
      contFromRxn: [''],
      projectCode: [''],
      contToRxn: [''],
      coAuthors: [''],
      literature: [''],
      projectAlias: [''],
      linkedExperiments: [''],
      batchCreators: [''],
    });
  }

  getDictionaries() {
    this.service.setup('dictionaries');
    this.theraputicItem$ = this.service
      .getDictionary<{ id: string; name: string }[]>('THERAPEUTIC_AREA')
      .pipe(map((items) => items.map((e) => ({ label: e.name, value: e.id }))));
    this.projectCodeItem$ = this.service
      .getDictionary<{ id: string; name: string }[]>('PROJECT_CODE')
      .pipe(map((items) => items.map((e) => ({ label: e.name, value: e.id }))));
  }

  addChip(event: any, field: 'linkedExperiments' | 'batchCreators') {
    if (this.formGroup.value[field].trim() && event.key === 'Enter') {
      this[field].push(this.formGroup.value[field].trim());
      this.formGroup.controls[field].setValue('');
    }
  }

  removeChip(index: number, field: 'linkedExperiments' | 'batchCreators') {
    this[field].splice(index, 1);
  }

  async open() {
    return await this.modalComponent().open();
  }

  close(reason) {
    this.formGroup.reset();
    this.isSubmitted = false;
    this.linkedExperiments = [];
    this.batchCreators = [];
    this.modalComponent().close(reason);
  }

  createExperiment() {
    this.isSubmitted = true;
    if (this.formGroup.valid) {
      const data = this.formGroup.value;
      data.linkedExperiments = this.linkedExperiments;
      data.batchCreators = this.batchCreators;
      this.service.setup('notebooks', {
        createUrl: '{notebookId}/experiments',
      });
      this.service.create(data).subscribe({
        next: (res: any) => {
          if (res?.id) {
            this.close('experimentAdded');
          }
        },
        error: (error) => {
          this.close('experimentAddError');
          alert(error.message); // Display error message to user
        },
      });
    }
  }
}
