import { Component, inject } from '@angular/core';
import { FieldType, FieldTypeConfig } from '@ngx-formly/core';
import { ReactiveFormsModule } from '@angular/forms';
import { AutocompleteSelectComponent } from '@core/components/common/autocomplete-select/autocomplete-select.component';
import { ApiService } from '@core/services/api.service';
import { ExperimentRef } from '@core/types/entities/experiments/experiment-shared.i';
import { Observable } from 'rxjs';

@Component({
  selector: 'eln-formly-experiment-select',
  standalone: true,
  imports: [AutocompleteSelectComponent, ReactiveFormsModule],
  template: `
    <eln-autocomplete-select [search]="search" [display]="display" [formControl]="formControl">
      <ng-template #optionTemplate let-item>
        <span>{{ item.name }}</span>
      </ng-template>
    </eln-autocomplete-select>
  `,
})
export class ExperimentSelectFieldComponent extends FieldType<FieldTypeConfig> {
  private api = inject(ApiService);

  search = (query: string): Observable<ExperimentRef[]> =>
    this.api.request<ExperimentRef[]>('get', `experiments/suggest?search=${encodeURIComponent(query)}`);

  display = (item: ExperimentRef): string => item?.name ?? '';
}
