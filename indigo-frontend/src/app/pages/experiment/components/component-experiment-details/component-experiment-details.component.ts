import {Component, inject, Input, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Experiment} from '@core/types/entities/experiment.i';
import {ExperimentService} from '@core/services/experiment.service';
import {FormlyFieldConfig, FormlyModule} from '@ngx-formly/core';
import {FormGroup} from '@angular/forms';
import {ApiService} from '@core/services/api.service';
import {Subject, takeUntil} from 'rxjs';
import {DictionaryItemRef} from '@core/types/entities/dictionary.i';

@Component({
  selector: 'eln-component-experiment-details',
  templateUrl: './component-experiment-details.component.html',
  imports: [FormlyModule],
})
export class ComponentExperimentDetailsComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  service = inject(ApiService)

  experiment: Experiment | null;

  fields: FormlyFieldConfig[] = [];
  form = new FormGroup({});

  private destroy$ = new Subject<void>();

  ngOnInit() {
    this.experimentService.experiment$
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
        this.loadOptionsFromDictionary(therapeuticAreaField, 'THERAPEUTIC_AREA');
        this.loadOptionsFromDictionary(projectCodeField, 'PROJECT_CODE');
      });
  }

  ngOnDestroy() {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadOptionsFromDictionary(fieldDef: any, dictionaryId: string) {
    this.service.request<DictionaryItemRef[]>('get', `dictionaries/${dictionaryId}`)
      .pipe(takeUntil(this.destroy$))
      .subscribe((list) => {
        fieldDef.props.options = list.map((x) => ({value: x.id, label: x.name,}));
      });
  }
}
