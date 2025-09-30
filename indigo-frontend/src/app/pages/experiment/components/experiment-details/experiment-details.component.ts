import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ExperimentDetail } from '@core/types/entities/experiments/experiment-detail.i';
import { ExperimentService } from '@core/services/experiment/experiment.service';
import { FormlyFieldConfig, FormlyModule } from '@ngx-formly/core';
import { FormGroup } from '@angular/forms';
import { ApiService } from '@core/services/api.service';
import { DictionaryItemRef } from '@core/types/entities/dictionary.i';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'eln-experiment-details',
  templateUrl: './experiment-details.component.html',
  imports: [FormlyModule],
})
export class ExperimentDetailsComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  service = inject(ApiService);

  destroyRef = inject(DestroyRef);

  experiment: ExperimentDetail | null;

  fields: FormlyFieldConfig[] = [];
  form = new FormGroup({});

  ngOnInit() {
    this.experimentService.experiment$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((experiment) => {
        this.experiment = experiment;
        const nameField = {
          type: 'input',
          key: 'name',
          defaultValue: this.experiment.name,
          props: {
            label: 'Experiment Number',
            placeholder: 'Experiment Number',
          },
        };
        const therapeuticAreaField = {
          type: 'select',
          key: 'therapeuticArea',
          defaultValue: this.experiment.therapeuticArea?.id,
          props: {
            label: 'Therapeutic Area',
            placeholder: 'Therapeutic Area',
            options: [],
          },
        };
        const projectCodeField = {
          type: 'select',
          key: 'projectCode',
          defaultValue: this.experiment.projectCode?.id,
          props: {
            label: 'Project Code',
            placeholder: 'Project Code',
            options: [],
          },
        };
        this.fields = [nameField, therapeuticAreaField, projectCodeField];
        this.loadOptionsFromDictionary(
          therapeuticAreaField,
          'THERAPEUTIC_AREA',
        );
        this.loadOptionsFromDictionary(projectCodeField, 'PROJECT_CODE');
      });
  }

  private loadOptionsFromDictionary(fieldDef: any, dictionaryId: string) {
    this.service
      .request<DictionaryItemRef[]>('get', `dictionaries/${dictionaryId}`)
      .subscribe((list) => {
        fieldDef.props.options = list.map((x) => ({
          value: x.id,
          label: x.name,
        }));
      });
  }
}
