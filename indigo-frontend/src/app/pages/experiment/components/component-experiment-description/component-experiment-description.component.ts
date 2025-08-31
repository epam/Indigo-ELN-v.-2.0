import {Component, inject, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Experiment} from '@core/types/entities/experiment.i';
import {ExperimentService} from '@core/services/experiment.service';
import {FormlyFieldConfig, FormlyModule} from '@ngx-formly/core';
import {FormGroup} from '@angular/forms';
import {filter, map} from 'rxjs';

@Component({
  selector: 'eln-component-experiment-description',
  templateUrl: './component-experiment-description.component.html',
  imports: [FormlyModule],
})
export class ComponentExperimentDescriptionComponent implements OnInit {
  activatedRoute = inject(ActivatedRoute);

  experimentService = inject(ExperimentService);

  experiment: Experiment | null;

  fields: FormlyFieldConfig[] = [];

  form = new FormGroup({});

  ngOnInit() {
    this.experimentService.experiment$
      .subscribe((experiment) => {
          this.experiment = experiment;
          this.fields = [
            {
              type: 'editor',
              key: 'description',
              defaultValue: experiment.description,
              props: {
                label: 'Description',
                placeholder: 'Description',
              },
            },
          ];
        });
  }
}
