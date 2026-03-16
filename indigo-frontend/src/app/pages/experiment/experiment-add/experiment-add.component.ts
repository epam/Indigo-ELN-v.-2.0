import { FormDialogComponent } from '@/core/components/common/form-dialog/form-dialog.component';
import { ApiService } from '@/core/services/api.service';
import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinner, MatSpinner } from '@angular/material/progress-spinner';
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
    MatProgressSpinner
  ],
  templateUrl: './experiment-add.component.html',
})
export class ExperimentAddComponent<T> implements OnInit {
  fields: FormlyFieldConfig[];
  private service = inject(ApiService);

  cdr = inject(ChangeDetectorRef);
  show: boolean;

  ngOnInit(): void {
    const nameField = {
      type: 'select',
        key: 'Select Template',
      className: 'flex-1',
      props: {
        label: 'Select Template',
        placeholder: 'Select Template',
        options: [],
      },
    };
    this.fields = [nameField];
    this.loadOptionsFromDictionary(nameField);
  }

  private loadOptionsFromDictionary(fieldDef: any) {
    this.service
      .request<any>('get', `templates`)
      .subscribe((list) => {
        fieldDef.props.options = list.items.map((x) => ({
          value: x.id,
          label: x.name,
        }));
        console.log(this.fields);
        this.show = true;
        this.cdr.detectChanges();
      });
  }

  createExperiment(data: any) {}
}
